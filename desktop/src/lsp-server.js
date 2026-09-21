// ═══════════════════════════════════════════════════════════
//  言語サーバー (LSP) の起動と中継
//
//  Java は Eclipse JDT Language Server (jdtls) を子プロセスとして起動する
//  jdtls は stdio 上で JSON-RPC を Content-Length ヘッダ付きでやり取りするので、
//  ここでフレーミングを外し、本文 (JSON 文字列) だけを IPC で renderer へ流す
//  renderer 側の受け口は src/renderer/editor/lsp.js
//
//  ── 配置 ────────────────────────────────────────────────
//    開発時   : <repo>/resources/jdtls        + <repo>/runtime/java
//    パッケージ: resources/jdtls              + resources/runtime/java
//  どちらも scripts/setup-jdtls.ps1 が用意する (jdtls は git 管理外)
//
//  ── 単位 ────────────────────────────────────────────────
//  サーバーは「ワークスペースルート (= セッションディレクトリ) ごとに 1 つ」
//  同じルートに対する 2 度目の start は既存のサーバーを使い回す
//  jdtls はメモリを数百 MB 使うため、同時に走らせるのは MAX_SERVERS 個までとし、
//  超えたら古いものから止める
// ═══════════════════════════════════════════════════════════

const fs    = require('fs');
const os    = require('os');
const path  = require('path');
const crypto = require('crypto');
const { spawn } = require('child_process');

// 同時に起動しておく言語サーバーの上限 (超過分は古いものから停止)
const MAX_SERVERS = 2;

const servers = new Map();   // id -> { id, proc, language, rootPath, lastUsed }
let seq = 0;

// ── 配置の解決 ─────────────────────────────────────────
// main.js と同じ規約 (app.isPackaged で resources 配下 / 開発時はリポジトリ直下)
function resolveDirs(app) {
  const repoRoot = path.join(__dirname, '..', '..');
  return app.isPackaged
    ? {
        jdtls:    path.join(process.resourcesPath, 'jdtls'),
        javaHome: path.join(process.resourcesPath, 'runtime', 'java'),
      }
    : {
        jdtls:    path.join(repoRoot, 'resources', 'jdtls'),
        javaHome: path.join(repoRoot, 'runtime', 'java'),
      };
}

function javaExe(javaHome) {
  const bundled = path.join(javaHome, 'bin', 'java.exe');
  return fs.existsSync(bundled) ? bundled : 'java';
}

/** jdtls の起動コマンドを組み立てる。同梱されていなければ null */
function jdtlsLaunch(app, rootPath) {
  const { jdtls, javaHome } = resolveDirs(app);
  const pluginsDir = path.join(jdtls, 'plugins');
  if (!fs.existsSync(pluginsDir)) return null;

  const launcher = fs.readdirSync(pluginsDir)
    .find(name => /^org\.eclipse\.equinox\.launcher_.*\.jar$/.test(name));
  if (!launcher) return null;

  // 設定ディレクトリは OS ごとに分かれている (Windows 版のみ同梱)
  const configDir = ['config_win', 'config_linux', 'config_mac']
    .map(d => path.join(jdtls, d))
    .find(d => fs.existsSync(d));
  if (!configDir) return null;

  // jdtls の作業データ (インデックス) はワークスペースの外に置く
  // 学習者のファイル一覧に .metadata が現れると紛らわしいため
  const hash = crypto.createHash('sha1').update(rootPath).digest('hex').slice(0, 16);
  const dataDir = path.join(os.tmpdir(), 'codinable-jdtls', hash);
  fs.mkdirSync(dataDir, { recursive: true });

  return {
    command: javaExe(javaHome),
    args: [
      '-Declipse.application=org.eclipse.jdt.ls.core.id1',
      '-Dosgi.bundles.defaultStartLevel=4',
      '-Declipse.product=org.eclipse.jdt.ls.core.product',
      '-Dlog.level=WARNING',
      '-Dfile.encoding=UTF-8',
      '-Xms128m', '-Xmx768m',
      '--add-modules=ALL-SYSTEM',
      '--add-opens', 'java.base/java.util=ALL-UNNAMED',
      '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
      '-jar', path.join(pluginsDir, launcher),
      '-configuration', configDir,
      '-data', dataDir,
    ],
    env: { ...process.env, JAVA_HOME: javaHome },
  };
}

/** stdout の Content-Length フレーミングを外してメッセージ単位に分解する */
function createFrameParser(onMessage) {
  let buffer = Buffer.alloc(0);
  return chunk => {
    buffer = Buffer.concat([buffer, chunk]);
    for (;;) {
      const headerEnd = buffer.indexOf('\r\n\r\n');
      if (headerEnd < 0) return;
      const header = buffer.slice(0, headerEnd).toString('ascii');
      const match  = header.match(/Content-Length:\s*(\d+)/i);
      if (!match) { buffer = buffer.slice(headerEnd + 4); continue; }
      const length = Number(match[1]);
      const total  = headerEnd + 4 + length;
      if (buffer.length < total) return;
      onMessage(buffer.slice(headerEnd + 4, total).toString('utf8'));
      buffer = buffer.slice(total);
    }
  };
}

/** この環境で LSP を使えるか (jdtls と JRE が揃っているか) */
function isAvailable(app, language = 'java') {
  if (language !== 'java') return false;
  return !!jdtlsLaunch(app, os.tmpdir());
}

/**
 * 言語サーバーを起動する (同じ rootPath のものがあれば使い回す)
 * @returns {{ok: true, id: string, reused: boolean} | {ok: false, error: string}}
 */
function start(app, sender, { language, rootPath }) {
  if (language !== 'java') return { ok: false, error: `unsupported language: ${language}` };
  if (!rootPath) return { ok: false, error: 'rootPath is required' };

  for (const entry of servers.values()) {
    if (entry.language === language && entry.rootPath === rootPath && !entry.proc.killed) {
      entry.lastUsed = Date.now();
      return { ok: true, id: entry.id, reused: true };
    }
  }

  const launch = jdtlsLaunch(app, rootPath);
  if (!launch) return { ok: false, error: 'jdtls is not bundled' };

  let proc;
  try {
    proc = spawn(launch.command, launch.args, {
      cwd: rootPath,
      env: launch.env,
      stdio: ['pipe', 'pipe', 'pipe'],
      windowsHide: true,
    });
  } catch (err) {
    return { ok: false, error: err.message };
  }

  const id = `lsp-${++seq}`;
  const entry = { id, proc, language, rootPath, lastUsed: Date.now() };
  servers.set(id, entry);

  const post = payload => {
    if (!sender.isDestroyed()) sender.send('lsp-message', payload);
  };

  proc.stdout.on('data', createFrameParser(body => post({ id, body })));
  proc.stdin.on('error', err => console.warn(`[lsp:${id}] stdin:`, err.message));
  // jdtls は起動時に大量の情報を stderr に出すので、警告以上だけ拾う
  proc.stderr.on('data', chunk => {
    const text = chunk.toString().trim();
    if (/error|exception/i.test(text)) console.warn(`[lsp:${id}] ${text.slice(0, 500)}`);
  });
  proc.on('error', err => {
    console.warn(`[lsp:${id}] spawn failed:`, err.message);
    servers.delete(id);
    post({ id, event: 'exit', error: err.message });
  });
  proc.on('exit', code => {
    console.log(`[lsp:${id}] exited (${code})`);
    servers.delete(id);
    post({ id, event: 'exit', code });
  });

  // 上限を超えたぶんは、最後に使われたのが古いものから止める
  if (servers.size > MAX_SERVERS) {
    const sorted = [...servers.values()].sort((a, b) => a.lastUsed - b.lastUsed);
    for (const old of sorted.slice(0, servers.size - MAX_SERVERS)) stop(old.id);
  }

  console.log(`[lsp:${id}] started (${language}) root=${rootPath}`);
  return { ok: true, id, reused: false };
}

/** renderer からのメッセージをフレーミングして子プロセスへ渡す */
function send({ id, body }) {
  const entry = servers.get(id);
  if (!entry || entry.proc.killed) return { ok: false };
  entry.lastUsed = Date.now();
  const buf = Buffer.from(String(body), 'utf8');
  try {
    entry.proc.stdin.write(`Content-Length: ${buf.length}\r\n\r\n`);
    entry.proc.stdin.write(buf);
  } catch (err) {
    return { ok: false, error: err.message };
  }
  return { ok: true };
}

function stop(id) {
  const entry = servers.get(id);
  if (!entry) return { ok: true };
  servers.delete(id);
  try { entry.proc.kill(); } catch { /* already dead */ }
  return { ok: true };
}

/** アプリ終了時に全部止める (居残りが $INSTDIR のファイルを掴んだままになるのを防ぐ) */
function stopAll() {
  for (const id of [...servers.keys()]) stop(id);
}

// jdtlsLaunch は OpenCode (AI エージェント) の lsp 設定でも使う (main.js buildAgentConfig)
module.exports = { start, send, stop, stopAll, isAvailable, jdtlsLaunch };
