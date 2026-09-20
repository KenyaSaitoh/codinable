// 子プロセスと出力の取り扱いに関する小道具。

const fs   = require('fs');
const path = require('path');
const { exec } = require('child_process');

const IS_WIN = process.platform === 'win32';

/**
 * 子プロセスの出力をテキストに変換する。
 * Windows の Java / Gradle は環境によって Shift-JIS で書き出すことがあるため、
 * UTF-8 として不正なら Shift-JIS で読み直す。
 */
function decodeOutput(buf) {
  if (!buf || !buf.length) return '';
  const utf8 = Buffer.isBuffer(buf) ? buf.toString('utf8') : String(buf);
  if (!utf8.includes('\uFFFD')) return utf8;
  try {
    return new TextDecoder('shift_jis').decode(buf);
  } catch {
    return utf8;
  }
}

/** プロセスツリーごと止める (Gradle デーモンや npm の孫プロセスを残さないため) */
function killTree(proc) {
  if (!proc || !proc.pid) return Promise.resolve();
  const pid = proc.pid;
  if (!IS_WIN) {
    try { process.kill(-pid, 'SIGKILL'); } catch { try { proc.kill('SIGKILL'); } catch {} }
    return Promise.resolve();
  }
  return new Promise(resolve => exec(`taskkill /F /T /PID ${pid}`, () => resolve()));
}

/** 指定ポートを掴んでいるプロセスを強制終了する。終了させた PID を返す */
function killPort(port) {
  if (!IS_WIN || !port) return Promise.resolve([]);
  return new Promise(resolve => {
    exec(`netstat -ano | findstr ":${port} "`, (err, stdout) => {
      if (err || !stdout.trim()) return resolve([]);
      const pids = new Set();
      for (const line of stdout.trim().split('\n')) {
        const parts = line.trim().split(/\s+/);
        const pid   = parts[parts.length - 1];
        if (/^\d+$/.test(pid) && pid !== '0') pids.add(pid);
      }
      if (!pids.size) return resolve([]);
      Promise.all([...pids].map(pid => new Promise(r => exec(`taskkill /F /PID ${pid}`, () => r(pid)))))
        .then(killed => setTimeout(() => resolve(killed), 500));
    });
  });
}

// ── ディレクトリ走査 ───────────────────────────────────────

// ビルド成果物・依存ディレクトリはファイルツリーに出さない
const SKIP_DIRS = new Set([
  // .codinable はアプリが作る作業用ディレクトリ (project.json / 単体 Java の
  // クラス出力 / JaCoCo の init script)。受講者が触るものではないので隠す。
  '.codinable',
  '.git', '.gradle', '.idea', '.vscode', '.settings', '.venv', 'venv',
  'build', 'out', 'target', 'bin', 'dist', 'node_modules', '__pycache__',
  '.next', '.cache', '.pytest_cache', '.mypy_cache', '.classpath-cache',
]);

// テキストとして開くファイル (これ以外はツリーに出すが編集は開かない)
const TEXT_EXTS = new Set([
  '.java', '.kt', '.groovy', '.gradle', '.properties', '.xml', '.yaml', '.yml',
  '.json', '.js', '.mjs', '.cjs', '.jsx', '.ts', '.tsx', '.html', '.htm', '.css',
  '.scss', '.sass', '.less', '.py', '.sh', '.bash', '.sql', '.md', '.markdown',
  '.txt', '.csv', '.tsv', '.env', '.gitignore', '.editorconfig', '.toml', '.ini',
  '.conf', '.cfg', '.jsp', '.vue', '.svelte', '.http', '.rest',
]);

function isTextFile(relPath) {
  const base = path.basename(relPath);
  if (!base.includes('.')) return true;                 // Dockerfile, Makefile など
  return TEXT_EXTS.has(path.extname(base).toLowerCase());
}

/**
 * ディレクトリツリーを走査して { path, dir } の配列を返す (path はルート相対・スラッシュ区切り)。
 * 大きなワークスペースで固まらないよう件数上限を設ける。
 */
function walkTree(root, { limit = 4000 } = {}) {
  const entries = [];
  if (!fs.existsSync(root)) return entries;

  const walk = (current, rel, depth) => {
    if (entries.length >= limit || depth > 12) return;
    let dirents;
    try { dirents = fs.readdirSync(current, { withFileTypes: true }); } catch { return; }
    dirents.sort((a, b) => {
      if (a.isDirectory() !== b.isDirectory()) return a.isDirectory() ? -1 : 1;
      return a.name.localeCompare(b.name);
    });
    for (const entry of dirents) {
      if (entries.length >= limit) return;
      if (entry.isDirectory() && (SKIP_DIRS.has(entry.name) || entry.name.startsWith('.git'))) continue;
      const relPath = rel ? `${rel}/${entry.name}` : entry.name;
      if (entry.isDirectory()) {
        entries.push({ path: relPath, dir: true });
        walk(path.join(current, entry.name), relPath, depth + 1);
      } else {
        entries.push({ path: relPath, dir: false, text: isTextFile(relPath) });
      }
    }
  };

  walk(root, '', 0);
  return entries;
}

/**
 * ルート配下に収まる絶対パスへ変換する ('..' や絶対パス指定による逸脱を防ぐ)。
 * 逸脱していれば null。
 */
function safeJoin(root, relPath) {
  const parts = String(relPath || '').replace(/\\/g, '/').split('/')
    .filter(p => p && p !== '.' && p !== '..');
  if (!parts.length) return null;
  const rootResolved = path.resolve(root);
  const full = path.resolve(rootResolved, ...parts);
  if (full !== rootResolved && !full.startsWith(rootResolved + path.sep)) return null;
  return full;
}

/** ディレクトリを再帰コピーする (既存ファイルは上書きしない) */
function copyDirSafe(src, dest) {
  let written = 0;
  if (!fs.existsSync(src)) return written;
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const from = path.join(src, entry.name);
    const to   = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      fs.mkdirSync(to, { recursive: true });
      written += copyDirSafe(from, to);
    } else if (!fs.existsSync(to)) {
      fs.mkdirSync(path.dirname(to), { recursive: true });
      fs.copyFileSync(from, to);
      written++;
    }
  }
  return written;
}

module.exports = {
  IS_WIN,
  SKIP_DIRS,
  decodeOutput,
  killTree,
  killPort,
  isTextFile,
  walkTree,
  safeJoin,
  copyDirSafe,
};
