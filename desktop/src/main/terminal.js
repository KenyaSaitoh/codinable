// ═══════════════════════════════════════════════════════════
//  ターミナル
//
//  同梱 bash (runtime/bash) を PTY (node-pty + ConPTY) で起動して
//  renderer の xterm.js につなぐ。Ctrl+C・TUI・対話 REPL がそのまま使える。
//
//  作業ディレクトリは選択中のプロジェクト。環境変数はユーザーの環境をそのまま
//  引き継ぎ、同梱の java / node / python を PATH の先頭に足す (runtimes.getDevEnv)。
//  学習用サンドボックスではなく開発環境なので、git など普段のツールも見える。
//
//  node-pty (ネイティブモジュール) が読めない環境では、行単位のパイプ接続に
//  フォールバックする。
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const { spawn } = require('child_process');

const { resolveTerminalShell, getDevEnv, resolveBashDir } = require('./runtimes');
const { decodeOutput, killTree } = require('./util');
const { getWorkspaceRoot } = require('./config');
const { resolveProjectDir } = require('./workspace');

let ptyLib = null;
try {
  ptyLib = require('@lydell/node-pty');
} catch (err) {
  console.warn('[term] node-pty unavailable — falling back to pipe terminal:', err.message);
}

let termPty     = null;   // PTY モード
let termProcess = null;   // パイプモード (フォールバック)
let termShell   = null;
let termCwd     = null;

function terminalEnv(shellName) {
  const env = getDevEnv();
  env.TERM = 'xterm-256color';
  if (shellName === 'bash') {
    // 同梱 bash の coreutils / curl を PATH に通す
    const bashDir = resolveBashDir();
    if (bashDir) {
      const extra = [path.join(bashDir, 'usr', 'bin'), path.join(bashDir, 'mingw64', 'bin')]
        .filter(dir => fs.existsSync(dir));
      if (extra.length) env.PATH = extra.join(path.delimiter) + path.delimiter + (env.PATH || '');
      const caBundle = path.join(bashDir, 'mingw64', 'etc', 'ssl', 'certs', 'ca-bundle.crt');
      if (fs.existsSync(caBundle)) env.CURL_CA_BUNDLE = caBundle;
    }
    env.LANG = env.LANG || 'ja_JP.UTF-8';
    // MSYS のパス変換で Windows 形式の引数が壊れるのを防ぐ
    env.MSYS_NO_PATHCONV = '1';
  }
  return env;
}

function resolveCwd(project) {
  const dir = project ? resolveProjectDir(project) : null;
  if (dir && fs.existsSync(dir)) return dir;
  const root = getWorkspaceRoot();
  try { fs.mkdirSync(root, { recursive: true }); } catch {}
  return root;
}

function isRunning() {
  return !!(termPty || termProcess);
}

/**
 * ターミナルを起動する。
 * すでに動いていて cwd も同じなら使い回す (タブを開き直しても履歴が消えない)。
 */
function start(event, { cols, rows, project } = {}) {
  const cwd = resolveCwd(project);

  if (isRunning()) {
    if (cwd === termCwd) {
      return { ok: true, shell: termShell, cwd: termCwd, pty: !!termPty, alreadyRunning: true };
    }
    stop();   // プロジェクトが切り替わったら開き直す
  }

  const { exe, args, name } = resolveTerminalShell();
  const env = terminalEnv(name);
  const wc  = event.sender;

  if (ptyLib) {
    try {
      termPty = ptyLib.spawn(exe, args, {
        name: 'xterm-256color',
        cols: Math.max(2, cols || 100),
        rows: Math.max(2, rows || 24),
        cwd,
        env,
      });
      termShell = name;
      termCwd   = cwd;
      termPty.onData(data => { if (!wc.isDestroyed()) wc.send('term-output', data); });
      termPty.onExit(({ exitCode }) => {
        termPty = null;
        if (!wc.isDestroyed()) wc.send('term-exit', { code: exitCode });
      });
      return { ok: true, shell: name, cwd, pty: true };
    } catch (err) {
      console.warn('[term] PTY start failed — falling back to pipe mode:', err.message);
      termPty = null;
    }
  }

  try {
    termProcess = spawn(exe, name === 'bash' ? [] : args, { cwd, env, windowsHide: true });
  } catch (err) {
    termProcess = null;
    return { ok: false, error: err.message };
  }
  termShell = name;
  termCwd   = cwd;

  const onData = chunk => { if (!wc.isDestroyed()) wc.send('term-output', decodeOutput(chunk)); };
  termProcess.stdout.on('data', onData);
  termProcess.stderr.on('data', onData);
  termProcess.on('error', err => {
    termProcess = null;
    if (!wc.isDestroyed()) wc.send('term-exit', { code: -1, error: err.message });
  });
  termProcess.on('close', code => {
    termProcess = null;
    if (!wc.isDestroyed()) wc.send('term-exit', { code });
  });

  return { ok: true, shell: name, cwd, pty: false };
}

function write(data) {
  if (termPty) {
    try { termPty.write(String(data)); } catch { /* すでに終了している */ }
    return;
  }
  if (!termProcess) return;
  try { termProcess.stdin.write(String(data)); } catch { /* すでに終了している */ }
}

function resize({ cols, rows } = {}) {
  if (termPty && cols > 0 && rows > 0) {
    try { termPty.resize(Math.floor(cols), Math.floor(rows)); } catch { /* 競合時は無視 */ }
  }
}

function stop() {
  if (termPty) {
    const p = termPty;
    termPty = null;
    try { p.kill(); } catch { /* すでに終了している */ }
  }
  if (termProcess) {
    const p = termProcess;
    termProcess = null;
    killTree(p);
  }
  termCwd = null;
  return { ok: true };
}

module.exports = { start, write, resize, stop, isRunning };
