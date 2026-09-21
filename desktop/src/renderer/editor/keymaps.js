// ═══════════════════════════════════════════════════════════
//  キーバインド (CodeMirror 6)
//
//  設定ダイアログの「キーバインド」は CM5 時代から default / vim / emacs / sublime の 4 種
//  vim / emacs は CM6 用の実装 (@replit/codemirror-*) がそのまま使える
//  sublime だけは CM6 版が存在しないため、CM5 の sublime キーマップのうち
//  実際に使われる範囲 (行操作・複数選択・コメント) を標準コマンドで組み直している
// ═══════════════════════════════════════════════════════════

import { keymap } from '@codemirror/view';
import {
  defaultKeymap, historyKeymap, indentWithTab,
  deleteLine, copyLineDown, copyLineUp, moveLineDown, moveLineUp,
  selectLine, insertBlankLine, toggleComment, indentMore, indentLess,
} from '@codemirror/commands';
import { searchKeymap, selectNextOccurrence } from '@codemirror/search';
import { completionKeymap, closeBracketsKeymap, acceptCompletion } from '@codemirror/autocomplete';
import { foldKeymap } from '@codemirror/language';
import { lintKeymap } from '@codemirror/lint';
import { vim } from '@replit/codemirror-vim';
import { emacs } from '@replit/codemirror-emacs';

export const KEYMAP_IDS = ['default', 'vim', 'emacs', 'sublime'];

// CM6 版が無いため、CM5 の sublime キーマップから実用的な範囲だけを再現する
const SUBLIME_KEYS = [
  { key: 'Mod-d',        run: selectNextOccurrence, preventDefault: true },
  { key: 'Mod-Shift-k',  run: deleteLine },
  { key: 'Mod-Shift-d',  run: copyLineDown },
  { key: 'Mod-Shift-ArrowUp',   run: copyLineUp },
  { key: 'Alt-ArrowUp',   run: moveLineUp },
  { key: 'Alt-ArrowDown', run: moveLineDown },
  { key: 'Mod-l',        run: selectLine },
  { key: 'Mod-Enter',    run: insertBlankLine },
  { key: 'Mod-/',        run: toggleComment },
  { key: 'Mod-]',        run: indentMore },
  { key: 'Mod-[',        run: indentLess },
];

/**
 * CM5 の extraKeys 表記 ('Ctrl-D' / 'Shift-Cmd-Z') を CM6 のキー名へ直す
 * CM6 は 1 文字キーを event.key と突き合わせるので、修飾子付きの英字は小文字にする
 * (大文字のままだと Shift 付き扱いになってしまう)
 */
function translateKey(cm5Key) {
  const parts = cm5Key.split('-');
  const base = parts.pop();
  const mods = parts.map(m => (m === 'Cmd' ? 'Meta' : m));
  const key = base.length === 1 ? base.toLowerCase() : base;
  return [...mods, key].join('-');
}

/** CM5 の extraKeys オブジェクト → CM6 の keymap 配列 (最優先で当てる) */
export function extraKeymap(extraKeys, controller) {
  if (!extraKeys) return [];
  const binds = Object.entries(extraKeys).map(([cm5Key, handler]) => ({
    key: translateKey(cm5Key),
    // CM5 のハンドラは cm を受け取る。互換のためコントローラを渡す
    run: () => { handler(controller); return true; },
  }));
  return keymap.of(binds);
}

/** 基本キーマップ。Tab は補完確定 → インデントの順で試す (LSP 補完と共存させる) */
export function baseKeymap() {
  return keymap.of([
    { key: 'Tab', run: acceptCompletion },
    ...closeBracketsKeymap,
    ...completionKeymap,
    ...searchKeymap,
    ...historyKeymap,
    ...foldKeymap,
    ...lintKeymap,
    ...defaultKeymap,
    indentWithTab,
  ]);
}

/**
 * 選択中のキーバインド。vim / emacs は各パッケージの拡張ごと差し替える
 * vim() は他のキーマップより前に置く必要がある (パッケージの指定)
 */
export function keymapExtension(name) {
  if (name === 'vim')    return vim();
  if (name === 'emacs')  return emacs();
  if (name === 'sublime') return keymap.of(SUBLIME_KEYS);
  return [];
}
