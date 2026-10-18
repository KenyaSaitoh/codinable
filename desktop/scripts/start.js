// ═══════════════════════════════════════════════════════════
//  開発用の起動 (npm start)
//
//  配布するのは全講座入りの 1 本だけなので、開発時も courses/ の全講座を読み込んで起動する
//  (講座ごとに絞り込む仕組みは持たない。受講者の画面と同じ状態で確かめる)
// ═══════════════════════════════════════════════════════════

const path     = require('path');
const { spawn } = require('child_process');
const electron = require('electron');

const child = spawn(electron, ['.'], {
  cwd: path.resolve(__dirname, '..'),
  stdio: 'inherit',
  windowsHide: false,
});
child.on('exit', code => process.exit(code ?? 0));
// Ctrl+C は Electron 側にも届くので、こちらは終了を待つだけにする
process.on('SIGINT', () => {});
