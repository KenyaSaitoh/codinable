// ═══════════════════════════════════════════════════════════
//  言語 (CodeMirror 6)
//
//  renderer.js は CM5 時代の MIME (`text/x-java` など) を `mode` として渡してくる。
//  呼び出し側を書き換えずに済むよう、その MIME をそのまま受けて CM6 の
//  LanguageSupport に解決する。cmModeFromFilename() が返す値がすべての入力。
//
//  公式パッケージが無い言語 (groovy / shell / C) は @codemirror/legacy-modes の
//  CM5 モードを StreamLanguage 経由で使う。ハイライトだけなので実用上は十分。
// ═══════════════════════════════════════════════════════════

import { StreamLanguage } from '@codemirror/language';
import { java } from '@codemirror/lang-java';
import { python } from '@codemirror/lang-python';
import { sql } from '@codemirror/lang-sql';
import { javascript } from '@codemirror/lang-javascript';
import { xml } from '@codemirror/lang-xml';
import { json } from '@codemirror/lang-json';
import { html } from '@codemirror/lang-html';
import { css } from '@codemirror/lang-css';
import { markdown } from '@codemirror/lang-markdown';
import { groovy } from '@codemirror/legacy-modes/mode/groovy';
import { shell } from '@codemirror/legacy-modes/mode/shell';
import { c } from '@codemirror/legacy-modes/mode/clike';
import { properties } from '@codemirror/legacy-modes/mode/properties';
import { yaml } from '@codemirror/legacy-modes/mode/yaml';

// 遅延生成 (使われない言語のパーサを起動時に組み立てない)
const FACTORY = {
  'text/x-java':      () => java(),
  'text/x-groovy':    () => StreamLanguage.define(groovy),
  'xml':              () => xml(),
  'text/x-sql':       () => sql(),
  'python':           () => python(),
  'text/typescript':  () => javascript({ typescript: true }),
  'text/javascript':  () => javascript(),
  'application/json': () => json(),
  'text/x-sh':        () => StreamLanguage.define(shell),
  'text/x-csrc':      () => StreamLanguage.define(c),
  // 静的 Web ページの教材で使う。html() は埋め込みの <script> / <style> も色分けする
  'text/html':        () => html(),
  'text/css':         () => css(),
  'text/x-properties': () => StreamLanguage.define(properties),
  'text/x-yaml':      () => StreamLanguage.define(yaml),
  'text/markdown':    () => markdown(),
  'text/plain':       () => [],
};

const CACHE = new Map();

/** CM5 互換の mode 文字列 → CM6 の言語拡張。未知の mode はプレーンテキスト。 */
export function languageExtension(mode) {
  const key = FACTORY[mode] ? mode : 'text/plain';
  if (!CACHE.has(key)) CACHE.set(key, FACTORY[key]());
  return CACHE.get(key);
}

/** LSP に渡す languageId (LSP 仕様の識別子)。対応外は null。 */
export function lspLanguageId(mode) {
  return ({
    'text/x-java': 'java',
    'python': 'python',
    'text/typescript': 'typescript',
    'text/javascript': 'javascript',
  })[mode] || null;
}
