// ═══════════════════════════════════════════════════════════
//  エディタバンドルのビルド (esbuild)
//
//  CodeMirror 6 と @codemirror/lsp-client は ESM でしか配布されておらず、
//  renderer.js のような素の <script> からは読めない。ここで 1 ファイルへまとめ、
//  IIFE (グローバル CM6) として src/renderer/vendor/cm6.js に出力する
//
//    npm run build:editor
//
//  npm scripts の start:* / build:* は先頭でこれを呼ぶので、
//  開発起動・インストーラビルドのどちらでも最新版が使われる
//  出力先は git 管理外 (.gitignore) だが、asar には含める (files: 'src/**')
// ═══════════════════════════════════════════════════════════

const path = require('path');
const fs   = require('fs');
const esbuild = require('esbuild');

const DESKTOP_ROOT = path.join(__dirname, '..');
const ENTRY  = path.join(DESKTOP_ROOT, 'src', 'renderer', 'editor', 'index.js');
const OUTDIR = path.join(DESKTOP_ROOT, 'src', 'renderer', 'vendor');
const OUTFILE = path.join(OUTDIR, 'cm6.js');

fs.mkdirSync(OUTDIR, { recursive: true });

esbuild.build({
  entryPoints: [ENTRY],
  outfile:     OUTFILE,
  bundle:      true,
  format:      'iife',
  globalName:  'CM6',
  // Electron 33 = Chromium 130。トランスパイルは実質不要だが明示しておく
  target:      'chrome130',
  platform:    'browser',
  minify:      true,
  sourcemap:   false,
  legalComments: 'none',
  logLevel:    'info',
}).then(() => {
  const size = fs.statSync(OUTFILE).size;
  console.log(`[build-editor] ${path.relative(DESKTOP_ROOT, OUTFILE)} (${(size / 1024).toFixed(0)} KB)`);
}).catch(err => {
  console.error('[build-editor] failed:', err?.message || err);
  process.exit(1);
});
