// ═══════════════════════════════════════════════════════════
//  同梱ランタイムの解決
//
//  リポジトリ構成 (開発時):
//    codinable/
//    ├── desktop/src/main/     ← __dirname
//    ├── runtime/java/         ← jlink で作った最小 JDK (javac 同梱)
//    ├── runtime/node/         ← portable Node.js (npm 同梱)
//    ├── runtime/python/       ← embeddable Python
//    ├── runtime/bash/         ← PortableGit から抜いた bash + coreutils + curl
//    ├── hsqldb/               ← HSQLDB jar
//    ├── resources/jdtls/      ← Java 言語サーバー
//    └── resources/gradle-wrapper/gradle-wrapper.jar
//
//  パッケージ後は process.resourcesPath 直下に runtime/ hsqldb/ jdtls/
//  gradle-wrapper/ が展開される (builder/electron-builder.js の extraResources)
//
//  同梱が無い場合は PATH 上のコマンド名にフォールバックするので、
//  scripts/setup-runtimes.ps1 を実行していない環境でも
//  ユーザーが自分で入れた Java / Node / Python があれば動く
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const os   = require('os');
const path = require('path');
const { app } = require('electron');

const IS_WIN = process.platform === 'win32';
const EXE    = IS_WIN ? '.exe' : '';

/** リポジトリルート (開発時のみ意味がある) */
function getRepoRoot() {
  return path.join(__dirname, '..', '..', '..');
}

function resolveRuntimeDir(name) {
  const base = app.isPackaged
    ? path.join(process.resourcesPath, 'runtime')
    : path.join(getRepoRoot(), 'runtime');
  return path.join(base, name);
}

/** パッケージ後は resources 直下、開発時はリポジトリ直下にあるディレクトリ */
function resolveBundledDir(name, devRelative) {
  return app.isPackaged
    ? path.join(process.resourcesPath, name)
    : path.join(getRepoRoot(), ...devRelative);
}

// ── Java ───────────────────────────────────────────────────

function resolveJavaHome() {
  const dir = resolveRuntimeDir('java');
  return fs.existsSync(path.join(dir, 'bin', 'java' + EXE)) ? dir : null;
}

/** java / javac / jar などの実行ファイル。同梱が無ければコマンド名 */
function resolveJavaTool(toolName) {
  const home = resolveJavaHome();
  if (home) return path.join(home, 'bin', toolName + EXE);
  return toolName;
}

// ── Node.js / npm ──────────────────────────────────────────

function resolveNode() {
  const bundled = path.join(resolveRuntimeDir('node'), 'node' + EXE);
  return fs.existsSync(bundled) ? bundled : 'node';
}

// npm はフルパスで解決する。裸の "npm.cmd" を shell 経由で起動すると
// %~dp0 が CWD に解決される環境があり、npm-prefix.js が見つからず失敗するため
function resolveNpm() {
  const bundled = path.join(resolveRuntimeDir('node'), IS_WIN ? 'npm.cmd' : 'npm');
  if (fs.existsSync(bundled)) return bundled;
  if (!IS_WIN) return 'npm';
  for (const dir of (process.env.PATH || '').split(path.delimiter)) {
    if (!dir) continue;
    try {
      const npmCmd = path.join(dir, 'npm.cmd');
      if (fs.existsSync(npmCmd) &&
          fs.existsSync(path.join(dir, 'node_modules', 'npm', 'bin', 'npm-cli.js'))) {
        return npmCmd;
      }
    } catch { /* PATH に壊れた要素がある場合は読み飛ばす */ }
  }
  return null;
}

/**
 * npm 本体 (npm-cli.js) の場所
 * npm.cmd を shell 経由で起動すると (1) 標準入力が cmd.exe に吸われて
 * 子プロセスに届かず、(2) Windows では .cmd の直接 spawn が EINVAL になる
 * node で npm-cli.js を直接動かせば両方とも起きない
 */
function resolveNpmCli() {
  const bundled = path.join(resolveRuntimeDir('node'),
                            'node_modules', 'npm', 'bin', 'npm-cli.js');
  if (fs.existsSync(bundled)) return bundled;
  const npm = resolveNpm();
  if (!npm) return null;
  const sibling = path.join(path.dirname(npm),
                            'node_modules', 'npm', 'bin', 'npm-cli.js');
  return fs.existsSync(sibling) ? sibling : null;
}

// ── Python ─────────────────────────────────────────────────

function resolvePython() {
  const bundled = path.join(resolveRuntimeDir('python'), 'python' + EXE);
  return fs.existsSync(bundled) ? bundled : (IS_WIN ? 'python' : 'python3');
}

// ── Bash (ターミナル) ──────────────────────────────────────

function resolveBashDir() {
  const bundled = resolveRuntimeDir('bash');
  return fs.existsSync(path.join(bundled, 'usr', 'bin', 'bash.exe')) ? bundled : null;
}

function resolveBash() {
  const dir = resolveBashDir();
  if (dir) return path.join(dir, 'usr', 'bin', 'bash.exe');
  if (!IS_WIN) return '/bin/bash';
  // フォールバック: インストール済みの Git Bash
  for (const base of [process.env.ProgramFiles,
                      process.env['ProgramFiles(x86)'],
                      process.env.LocalAppData && path.join(process.env.LocalAppData, 'Programs')]) {
    if (!base) continue;
    const candidate = path.join(base, 'Git', 'bin', 'bash.exe');
    if (fs.existsSync(candidate)) return candidate;
  }
  return null;
}

// ── HSQLDB / Gradle Wrapper / jdtls ────────────────────────

function resolveHsqldb() {
  const dir = resolveBundledDir('hsqldb', ['hsqldb']);
  const jar = path.join(dir, 'hsqldb-2.7.3.jar');
  if (fs.existsSync(jar)) return jar;
  if (fs.existsSync(dir)) {
    const jars = fs.readdirSync(dir).filter(f => f.startsWith('hsqldb') && f.endsWith('.jar'));
    if (jars.length) return path.join(dir, jars[0]);
  }
  // フォールバック: Gradle のモジュールキャッシュ
  const cache = path.join(os.homedir(), '.gradle', 'caches', 'modules-2', 'files-2.1',
                          'org.hsqldb', 'hsqldb');
  if (fs.existsSync(cache)) {
    for (const ver of fs.readdirSync(cache).sort().reverse()) {
      const verDir = path.join(cache, ver);
      for (const hash of fs.readdirSync(verDir)) {
        const candidate = path.join(verDir, hash, `hsqldb-${ver}.jar`);
        if (fs.existsSync(candidate)) return candidate;
      }
    }
  }
  return null;
}

function resolveGradleWrapperJar() {
  const jar = path.join(resolveBundledDir('gradle-wrapper', ['resources', 'gradle-wrapper']),
                        'gradle-wrapper.jar');
  return fs.existsSync(jar) ? jar : null;
}

// ── 実行時の環境変数 ───────────────────────────────────────

const { applyJavaLocaleToEnv, getJavaRuntimeOptions, getJavacRuntimeOptions } =
  require('../java-locale');

/**
 * 開発作業用の環境変数
 * ユーザーの環境をそのまま引き継ぎ、同梱ランタイムを PATH の先頭に足す
 * (Codinable は学習用サンドボックスではなく開発環境なので、git や
 *  ユーザーが入れたツールがそのまま見えるほうが望ましい)
 */
function getDevEnv(uiLang = null, options = {}) {
  let env = { ...process.env, ...require('../messaging-config').connectionEnv() };

  // 開発時に npm 経由で Codinable を起動すると npm_config_* が子に継がれ、
  // 同梱 npm が "Unknown env config" を警告する。受講者にはエラーに見えるので落とす
  for (const key of Object.keys(env)) {
    if (key.toLowerCase().startsWith('npm_config_')) delete env[key];
  }

  const prepend = [];
  const javaHome = resolveJavaHome();
  if (javaHome) {
    env.JAVA_HOME = javaHome;
    prepend.push(path.join(javaHome, 'bin'));
  }
  for (const name of ['node', 'python']) {
    const dir = resolveRuntimeDir(name);
    if (fs.existsSync(dir)) prepend.push(dir);
  }
  const pyScripts = path.join(resolveRuntimeDir('python'), 'Scripts');
  if (fs.existsSync(pyScripts)) prepend.push(pyScripts);

  if (prepend.length) {
    env.PATH = prepend.join(path.delimiter) + path.delimiter + (env.PATH || '');
  }

  // Windows: System32 が PATH から落ちていると Gradle / npm が動かない
  if (IS_WIN) {
    const sysRoot = process.env.SystemRoot || process.env.WINDIR || 'C:\\Windows';
    if (!(env.PATH || '').toLowerCase().includes('system32')) {
      env.PATH = [path.join(sysRoot, 'System32'), sysRoot, env.PATH || ''].join(path.delimiter);
    }
  }

  env.PYTHONIOENCODING = 'utf-8';
  env.PYTHONUTF8 = '1';

  if (uiLang) env = applyJavaLocaleToEnv(env, uiLang, options);
  return env;
}

/** ターミナル用シェル */
function resolveTerminalShell() {
  if (!IS_WIN) return { exe: '/bin/bash', args: ['-i'], name: 'bash' };
  const bash = resolveBash();
  if (bash) return { exe: bash, args: ['-i'], name: 'bash' };
  return { exe: 'cmd.exe', args: ['/Q', '/K', 'prompt $G'], name: 'cmd' };
}

/** 設定パネルに出す同梱ランタイムのバージョン情報 */
function probeRuntimes(decodeOutput) {
  const { execFile } = require('child_process');
  const env = getDevEnv();
  const probe = (exe, args) => new Promise(resolve => {
    if (!exe) return resolve(null);
    // Windows の .cmd / .bat は shell 経由でしか起動できない (直接 spawn すると
    // EINVAL)。npm.cmd がここに来るので、拡張子を見て切り替える
    const viaShell = IS_WIN && /\.(cmd|bat)$/i.test(exe);
    const command  = viaShell ? `"${exe}" ${args.join(' ')}` : exe;
    const options  = { timeout: 6000, encoding: 'buffer', env, windowsHide: true };
    const done = (err, stdout, stderr) => {
      if (err) return resolve(null);
      const text = (decodeOutput(stdout) || decodeOutput(stderr)).trim();
      resolve(text.split('\n')[0] || null);
    };
    // spawn は同期的に投げることがある (EINVAL など)。1 つの失敗で
    // 設定パネル全体が出なくなると原因が見えないので、ここで閉じ込める
    try {
      if (viaShell) execFile(command, { ...options, shell: true }, done);
      else          execFile(exe, args, options, done);
    } catch { resolve(null); }
  });

  return Promise.all([
    probe(resolveJavaTool('java'), ['-version']),
    probe(resolveNode(), ['--version']),
    probe(resolveNpm(), ['--version']),
    probe(resolvePython(), ['--version']),
    probe(resolveBash(), ['--version']),
  ]).then(([java, node, npm, python, bash]) => ({
    java, node, npm, python, bash,
    hsqldb: !!resolveHsqldb(),
    gradleWrapper: !!resolveGradleWrapperJar(),
    ...Object.fromEntries(['kafka', 'rabbitmq', 'erlang'].map(name => {
      try { return [name, fs.readFileSync(path.join(resolveRuntimeDir(name), 'codinable-version.txt'), 'utf8').trim()]; }
      catch { return [name, null]; }
    })),
  }));
}

module.exports = {
  IS_WIN,
  EXE,
  getRepoRoot,
  resolveRuntimeDir,
  resolveBundledDir,
  resolveJavaHome,
  resolveJavaTool,
  resolveNode,
  resolveNpm,
  resolveNpmCli,
  resolvePython,
  resolveBash,
  resolveBashDir,
  resolveHsqldb,
  resolveGradleWrapperJar,
  getDevEnv,
  resolveTerminalShell,
  probeRuntimes,
  getJavaRuntimeOptions,
  getJavacRuntimeOptions,
};
