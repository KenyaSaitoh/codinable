// ═══════════════════════════════════════════════════════════
//  エディタバンドルのエントリ (window.CM6)
//
//  renderer.js はバンドラを通さない素のスクリプトなので、CodeMirror 6 (ESM のみ) を
//  そのまま <script> で読むことはできない。builder/build-editor.js が esbuild で
//  この 1 ファイルへまとめ、IIFE として window.CM6 に載せる
//    出力: src/renderer/vendor/cm6.js
// ═══════════════════════════════════════════════════════════

import { create, fromTextarea, CmController } from './controller.js';
import { THEME_IDS, DEFAULT_THEME_ID, isDarkTheme } from './themes.js';
import { KEYMAP_IDS } from './keymaps.js';
import { lspLanguageId } from './languages.js';
import { createLspConnection, pathToUri } from './lsp.js';

export {
  create,
  fromTextarea,
  CmController,
  THEME_IDS,
  DEFAULT_THEME_ID,
  isDarkTheme,
  KEYMAP_IDS,
  lspLanguageId,
  createLspConnection,
  pathToUri,
};
