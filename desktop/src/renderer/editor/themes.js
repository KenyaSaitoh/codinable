// ═══════════════════════════════════════════════════════════
//  エディタテーマ (CodeMirror 6)
//
//  CodeMirror 5 時代は cm-themes.css が `.cm-s-<id>` の CSS で色を当てていたが、
//  CM6 のテーマは JS オブジェクト (EditorView.theme + HighlightStyle) なので
//  ここに移した。id は renderer.js の THEMES と 1:1 で対応する
//
//  配色は移行前の cm-themes.css をそのまま引き継いでいる
//  (Dracula だけは CM5 同梱テーマを使っていたので、公式パレットから起こした)
//  CM5 のトークン名 → CM6 の lezer タグの対応は buildHighlight() を参照
// ═══════════════════════════════════════════════════════════

import { EditorView } from '@codemirror/view';
import { HighlightStyle, syntaxHighlighting } from '@codemirror/language';
import { tags as t } from '@lezer/highlight';

// 1 テーマ = 「枠 (chrome)」と「トークン色 (token)」
// chrome: bg / fg / gutterBg / gutterFg / gutterBorder / cursor / selection /
//         activeLine / matchingBracketOutline
const THEME_DEFS = {
  'github-dark': {
    dark: true,
    chrome: {
      bg: '#0d1117', fg: '#e6edf3',
      gutterBg: '#0d1117', gutterFg: '#6e7681', gutterBorder: '#21262d',
      cursor: '#e6edf3', selection: 'rgba(31,111,235,0.35)',
      activeLine: '#161b22', bracket: '#8b949e',
    },
    token: {
      comment: '#8b949e', string: '#a5d6ff', number: '#79c0ff', keyword: '#ff7b72',
      operator: '#ff7b72', atom: '#79c0ff', def: '#d2a8ff', variable: '#e6edf3',
      variable2: '#79c0ff', type: '#ffa657', property: '#79c0ff', meta: '#d2a8ff',
      builtin: '#ffa657', tag: '#7ee787', attribute: '#79c0ff', heading: '#79c0ff',
      link: '#a5d6ff', invalid: '#ffa198',
    },
  },
  'one-dark-pro': {
    dark: true,
    chrome: {
      bg: '#282c34', fg: '#abb2bf',
      gutterBg: '#282c34', gutterFg: '#495162', gutterBorder: '#3e4452',
      cursor: '#528bff', selection: '#3e4451',
      activeLine: '#2c313c', bracket: '#7f848e',
    },
    token: {
      comment: '#5c6370', commentItalic: true, string: '#98c379', number: '#d19a66',
      keyword: '#c678dd', operator: '#56b6c2', atom: '#d19a66', def: '#61afef',
      variable: '#e06c75', variable2: '#abb2bf', type: '#e5c07b', property: '#e06c75',
      meta: '#61afef', builtin: '#e5c07b', tag: '#e06c75', attribute: '#d19a66',
      heading: '#61afef', link: '#98c379', invalid: '#f44747',
    },
  },
  'tokyo-night': {
    dark: true,
    chrome: {
      bg: '#1a1b26', fg: '#a9b1d6',
      gutterBg: '#1a1b26', gutterFg: '#3b4261', gutterBorder: '#292e42',
      cursor: '#c0caf5', selection: '#33467c',
      activeLine: '#292e42', bracket: '#565f89',
    },
    token: {
      comment: '#565f89', string: '#9ece6a', number: '#ff9e64', keyword: '#bb9af7',
      operator: '#89ddff', atom: '#ff9e64', def: '#7aa2f7', variable: '#c0caf5',
      variable2: '#7dcfff', type: '#2ac3de', property: '#7dcfff', meta: '#7aa2f7',
      builtin: '#2ac3de', tag: '#f7768e', attribute: '#bb9af7', heading: '#7aa2f7',
      link: '#9ece6a', invalid: '#f7768e',
    },
  },
  // CM5 では codemirror 同梱の theme/dracula.css を使っていた。公式パレットから起こす
  dracula: {
    dark: true,
    chrome: {
      bg: '#282a36', fg: '#f8f8f2',
      gutterBg: '#282a36', gutterFg: '#6272a4', gutterBorder: '#3a3d4e',
      cursor: '#f8f8f0', selection: '#44475a',
      activeLine: '#313442', bracket: '#6272a4',
    },
    token: {
      comment: '#6272a4', string: '#f1fa8c', number: '#bd93f9', keyword: '#ff79c6',
      operator: '#ff79c6', atom: '#bd93f9', def: '#50fa7b', variable: '#f8f8f2',
      variable2: '#8be9fd', type: '#8be9fd', typeItalic: true, property: '#66d9ef',
      meta: '#f8f8f2', builtin: '#8be9fd', tag: '#ff79c6', attribute: '#50fa7b',
      heading: '#bd93f9', link: '#f1fa8c', invalid: '#ff5555',
    },
  },
  'github-light': {
    dark: false,
    chrome: {
      bg: '#ffffff', fg: '#1f2328',
      gutterBg: '#ffffff', gutterFg: '#8c959f', gutterBorder: '#d0d7de',
      cursor: '#1f2328', selection: 'rgba(9,105,218,0.22)',
      activeLine: '#f6f8fa', bracket: '#656d76',
    },
    token: {
      comment: '#6e7781', string: '#0a3069', number: '#0550ae', keyword: '#cf222e',
      operator: '#cf222e', atom: '#0550ae', def: '#8250df', variable: '#1f2328',
      variable2: '#0550ae', type: '#953800', property: '#0550ae', meta: '#8250df',
      builtin: '#953800', tag: '#116329', attribute: '#0550ae', heading: '#0550ae',
      link: '#0a3069', invalid: '#82071e',
    },
  },
  'solarized-light': {
    dark: false,
    chrome: {
      bg: '#fdf6e3', fg: '#657b83',
      gutterBg: '#fdf6e3', gutterFg: '#93a1a1', gutterBorder: '#eee8d5',
      cursor: '#657b83', selection: '#dfd9c3',
      activeLine: '#eee8d5', bracket: '#93a1a1',
    },
    token: {
      comment: '#93a1a1', commentItalic: true, string: '#2aa198', number: '#d33682',
      keyword: '#859900', operator: '#859900', atom: '#cb4b16', def: '#268bd2',
      variable: '#657b83', variable2: '#268bd2', type: '#b58900', property: '#268bd2',
      meta: '#268bd2', builtin: '#b58900', tag: '#268bd2', attribute: '#b58900',
      heading: '#b58900', link: '#2aa198', invalid: '#dc322f',
    },
  },
};

export const THEME_IDS = Object.keys(THEME_DEFS);
export const DEFAULT_THEME_ID = 'github-dark';

/** CM5 のトークン名 → CM6 (lezer) のタグ。cm-themes.css の対応をそのまま移した */
function buildHighlight(token) {
  const rules = [
    { tag: [t.lineComment, t.blockComment, t.docComment, t.comment],
      color: token.comment, fontStyle: token.commentItalic ? 'italic' : undefined },
    { tag: [t.string, t.special(t.string), t.regexp, t.character], color: token.string },
    { tag: [t.number, t.integer, t.float], color: token.number },
    { tag: [t.keyword, t.controlKeyword, t.definitionKeyword, t.moduleKeyword,
            t.operatorKeyword, t.modifier, t.self], color: token.keyword },
    { tag: [t.operator, t.derefOperator, t.compareOperator, t.logicOperator,
            t.arithmeticOperator, t.bitwiseOperator], color: token.operator },
    { tag: [t.atom, t.bool, t.null], color: token.atom },
    { tag: [t.definition(t.variableName), t.function(t.definition(t.variableName)),
            t.function(t.variableName), t.macroName], color: token.def },
    { tag: [t.variableName, t.name], color: token.variable },
    { tag: [t.special(t.variableName), t.local(t.variableName)], color: token.variable2 },
    { tag: [t.typeName, t.className, t.namespace, t.annotation],
      color: token.type, fontStyle: token.typeItalic ? 'italic' : undefined },
    { tag: [t.propertyName, t.definition(t.propertyName)], color: token.property },
    { tag: [t.meta, t.processingInstruction, t.documentMeta], color: token.meta },
    { tag: [t.standard(t.variableName), t.standard(t.typeName)], color: token.builtin },
    { tag: [t.tagName], color: token.tag },
    { tag: [t.attributeName], color: token.attribute },
    { tag: [t.heading], color: token.heading, fontWeight: 'bold' },
    { tag: [t.link, t.url], color: token.link, textDecoration: 'underline' },
    { tag: [t.invalid], color: token.invalid },
    { tag: [t.strong], fontWeight: 'bold' },
    { tag: [t.emphasis], fontStyle: 'italic' },
  ];
  // undefined のプロパティは CM6 の style-mod が出力しないよう落としておく
  const cleaned = rules.map(rule => {
    const out = {};
    for (const [k, v] of Object.entries(rule)) if (v !== undefined) out[k] = v;
    return out;
  });
  return HighlightStyle.define(cleaned);
}

/**
 * 枠まわりのテーマ。フォントは CSS 変数 (--editor-font-family / --editor-font-size) が正で、
 * 設定ダイアログのスライダーがそこを動かす。CM6 側では変数を参照するだけにする
 */
function buildChrome(c, dark) {
  return EditorView.theme({
    '&': {
      backgroundColor: c.bg,
      color: c.fg,
      height: '100%',
      fontSize: 'var(--editor-font-size, 15px)',
    },
    '.cm-content': {
      fontFamily: 'var(--editor-font-family, monospace)',
      caretColor: c.cursor,
    },
    '.cm-scroller': {
      fontFamily: 'var(--editor-font-family, monospace)',
      lineHeight: '1.5',
      overflow: 'auto',
    },
    '.cm-cursor, .cm-dropCursor': { borderLeftColor: c.cursor },
    '&.cm-focused .cm-cursor': { borderLeftColor: c.cursor },
    // 選択範囲は .cm-selectionBackground (非フォーカス時も含む) を塗る
    '.cm-selectionBackground, .cm-content ::selection': { backgroundColor: c.selection },
    '&.cm-focused .cm-selectionBackground': { backgroundColor: c.selection },
    '.cm-activeLine': { backgroundColor: c.activeLine },
    '.cm-activeLineGutter': { backgroundColor: c.activeLine, color: c.fg },
    '.cm-gutters': {
      backgroundColor: c.gutterBg,
      color: c.gutterFg,
      borderRight: `1px solid ${c.gutterBorder}`,
    },
    '.cm-lineNumbers .cm-gutterElement': { color: c.gutterFg },
    '.cm-matchingBracket, &.cm-focused .cm-matchingBracket': {
      backgroundColor: 'transparent',
      outline: `1px solid ${c.bracket}`,
    },
    '.cm-nonmatchingBracket': { outline: '1px solid #f14c4c' },
    '.cm-foldPlaceholder': {
      backgroundColor: 'transparent',
      border: `1px solid ${c.gutterBorder}`,
      color: c.gutterFg,
    },
    // 補完・ホバー・診断のポップアップ (LSP)
    '.cm-tooltip': {
      backgroundColor: c.gutterBg,
      border: `1px solid ${c.gutterBorder}`,
      color: c.fg,
    },
    '.cm-tooltip .cm-tooltip-arrow:before': { borderTopColor: c.gutterBorder, borderBottomColor: c.gutterBorder },
    '.cm-tooltip .cm-tooltip-arrow:after': { borderTopColor: c.gutterBg, borderBottomColor: c.gutterBg },
    '.cm-tooltip-autocomplete > ul > li[aria-selected]': {
      backgroundColor: c.selection,
      color: c.fg,
    },
    '.cm-panels': { backgroundColor: c.gutterBg, color: c.fg },
  }, { dark });
}

const CACHE = new Map();

/** テーマ id → CM6 拡張 (枠 + ハイライト)。生成結果は使い回す */
export function themeExtension(id) {
  const key = THEME_DEFS[id] ? id : DEFAULT_THEME_ID;
  if (!CACHE.has(key)) {
    const def = THEME_DEFS[key];
    CACHE.set(key, [
      buildChrome(def.chrome, def.dark),
      syntaxHighlighting(buildHighlight(def.token)),
    ]);
  }
  return CACHE.get(key);
}

export function isDarkTheme(id) {
  return !!(THEME_DEFS[id] || THEME_DEFS[DEFAULT_THEME_ID]).dark;
}
