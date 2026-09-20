// ═══════════════════════════════════════════════════════════
//  CodeMirror 6 コントローラ (CM5 互換シム)
//
//  renderer.js は CodeMirror 5 の API (setOption / getValue / addLineClass …) を
//  6,000 行超にわたって直接呼んでいる。CM6 への移行でその全部を書き換えると
//  差分が大きく壊しやすいので、**実際に使われている API だけ**を CM6 の上に
//  再実装したコントローラを挟む。renderer.js 側の変更は生成箇所だけで済む。
//
//  再現している API:
//    getValue / setValue / getOption / setOption(readOnly, mode, theme, keyMap)
//    on('change' | 'changes' | 'optionChange' | 'focus') / focus / refresh
//    undo / redo / historySize / clearHistory / lineCount / operation
//    addLineClass / removeLineClass / scrollIntoView / execCommand
//
//  行番号は CM5 に合わせて 0 始まりで受け渡しする。
// ═══════════════════════════════════════════════════════════

import { EditorState, Compartment } from '@codemirror/state';
import {
  EditorView, lineNumbers, highlightActiveLine, highlightActiveLineGutter,
  drawSelection, dropCursor, rectangularSelection, crosshairCursor,
  highlightSpecialChars,
} from '@codemirror/view';
import {
  history, undo as undoCmd, redo as redoCmd, undoDepth, redoDepth, deleteLine,
} from '@codemirror/commands';
import {
  indentUnit, bracketMatching, foldGutter, indentOnInput, syntaxHighlighting,
  defaultHighlightStyle,
} from '@codemirror/language';
import { closeBrackets } from '@codemirror/autocomplete';
import { highlightSelectionMatches } from '@codemirror/search';
import { lintGutter } from '@codemirror/lint';

import { themeExtension, DEFAULT_THEME_ID } from './themes.js';
import { languageExtension, lspLanguageId } from './languages.js';
import { baseKeymap, keymapExtension, extraKeymap, KEYMAP_IDS } from './keymaps.js';
import {
  lineClassField, addLineClassEffect, removeLineClassEffect,
} from './line-classes.js';

const EVENTS = ['change', 'changes', 'optionChange', 'focus'];

export class CmController {
  constructor(parent, opts = {}) {
    this.opts = {
      mode:            opts.mode || 'text/plain',
      theme:           opts.theme || DEFAULT_THEME_ID,
      keyMap:          KEYMAP_IDS.includes(opts.keyMap) ? opts.keyMap : 'default',
      readOnly:        !!opts.readOnly,
      lineNumbers:     opts.lineNumbers !== false,
      lineWrapping:    !!opts.lineWrapping,
      indentUnit:      opts.indentUnit || 4,
      tabSize:         opts.tabSize || 4,
    };
    this.handlers = Object.fromEntries(EVENTS.map(e => [e, []]));

    this.c = {
      language: new Compartment(),
      theme:    new Compartment(),
      keymap:   new Compartment(),
      readOnly: new Compartment(),
      history:  new Compartment(),
      wrap:     new Compartment(),
      lsp:      new Compartment(),
    };

    const o = this.opts;
    this.view = new EditorView({
      parent,
      state: EditorState.create({
        doc: opts.value || '',
        extensions: [
          // extraKeys は CM5 と同じく標準キーマップより優先させる
          extraKeymap(opts.extraKeys, this),
          this.c.keymap.of(keymapExtension(o.keyMap)),
          baseKeymap(),

          o.lineNumbers ? [lineNumbers(), highlightActiveLineGutter()] : [],
          highlightSpecialChars(),
          this.c.history.of(history()),
          foldGutter(),
          drawSelection(),
          dropCursor(),
          EditorState.allowMultipleSelections.of(true),
          indentOnInput(),
          bracketMatching(),
          closeBrackets(),
          rectangularSelection(),
          crosshairCursor(),
          highlightActiveLine(),
          highlightSelectionMatches(),
          lintGutter(),
          // テーマが色を上書きするが、未知トークンの既定色として残す
          syntaxHighlighting(defaultHighlightStyle, { fallback: true }),

          indentUnit.of(' '.repeat(o.indentUnit)),
          EditorState.tabSize.of(o.tabSize),
          this.c.wrap.of(o.lineWrapping ? EditorView.lineWrapping : []),
          this.c.language.of(languageExtension(o.mode)),
          this.c.theme.of(themeExtension(o.theme)),
          this.c.readOnly.of(EditorState.readOnly.of(o.readOnly)),
          this.c.lsp.of([]),
          lineClassField,

          EditorView.updateListener.of(update => {
            if (update.docChanged) {
              this.emit('change');
              this.emit('changes');
            }
            if (update.focusChanged && update.view.hasFocus) this.emit('focus');
          }),
        ],
      }),
    });
  }

  // ── イベント ──────────────────────────────
  on(event, handler) {
    if (this.handlers[event]) this.handlers[event].push(handler);
  }

  emit(event, ...args) {
    for (const handler of this.handlers[event] || []) handler(this, ...args);
  }

  // ── 内容 ────────────────────────────────
  getValue() { return this.view.state.doc.toString(); }

  setValue(value) {
    const text = value == null ? '' : String(value);
    this.view.dispatch({
      changes: { from: 0, to: this.view.state.doc.length, insert: text },
      selection: { anchor: 0 },
    });
  }

  lineCount() { return this.view.state.doc.lines; }

  // ── オプション ────────────────────────────
  getOption(name) { return this.opts[name]; }

  setOption(name, value) {
    if (this.opts[name] === value) return;
    this.opts[name] = value;
    switch (name) {
      case 'mode':
        this.view.dispatch({ effects: this.c.language.reconfigure(languageExtension(value)) });
        break;
      case 'theme':
        this.view.dispatch({ effects: this.c.theme.reconfigure(themeExtension(value)) });
        break;
      case 'keyMap':
        this.view.dispatch({ effects: this.c.keymap.reconfigure(keymapExtension(value)) });
        break;
      case 'readOnly':
        this.view.dispatch({
          effects: this.c.readOnly.reconfigure(EditorState.readOnly.of(!!value)),
        });
        break;
      case 'lineWrapping':
        this.view.dispatch({
          effects: this.c.wrap.reconfigure(value ? EditorView.lineWrapping : []),
        });
        break;
      default:
        break;
    }
    this.emit('optionChange', name);
  }

  /** LSP プラグイン (ファイル単位) の差し替え。未接続時は空配列を渡す。 */
  setLspExtension(extension) {
    this.view.dispatch({ effects: this.c.lsp.reconfigure(extension || []) });
  }

  /** いま開いている mode に対応する LSP の languageId (対応外は null) */
  lspLanguageId() { return lspLanguageId(this.opts.mode); }

  // ── 履歴 ────────────────────────────────
  historySize() {
    return { undo: undoDepth(this.view.state), redo: redoDepth(this.view.state) };
  }

  clearHistory() {
    // history() を作り直すと StateField が初期化され、CM5 の clearHistory と同じ結果になる
    this.view.dispatch({ effects: this.c.history.reconfigure(history()) });
  }

  undo() { undoCmd(this.view); }
  redo() { redoCmd(this.view); }

  // ── 表示 ────────────────────────────────
  focus()   { this.view.focus(); }
  refresh() { this.view.requestMeasure(); }

  operation(fn) { return fn(); }   // CM6 はトランザクションで自動的にまとまる

  scrollIntoView(pos, margin) {
    const state = this.view.state;
    const line  = Math.min(Math.max((pos?.line ?? 0) + 1, 1), state.doc.lines);
    const at    = state.doc.line(line).from + (pos?.ch || 0);
    this.view.dispatch({
      effects: EditorView.scrollIntoView(Math.min(at, state.doc.length), {
        y: 'center', yMargin: margin || 0,
      }),
    });
  }

  // ── 行クラス (カバレッジ・講義ハイライト) ──────
  addLineClass(line, _where, cls) {
    this.view.dispatch({ effects: addLineClassEffect.of({ line, cls }) });
  }

  removeLineClass(line, _where, cls) {
    this.view.dispatch({ effects: removeLineClassEffect.of({ line, cls }) });
  }

  // ── コマンド ─────────────────────────────
  execCommand(name) {
    if (name === 'deleteLine') { deleteLine(this.view); return; }
    if (name === 'insertSoftTab') {
      // 次のタブストップまでスペースを入れる (CM5 の insertSoftTab 相当)
      const state = this.view.state;
      const width = this.opts.tabSize;
      this.view.dispatch(state.changeByRange(range => {
        const col = range.head - state.doc.lineAt(range.head).from;
        const insert = ' '.repeat(width - (col % width) || width);
        return {
          changes: { from: range.from, to: range.to, insert },
          range: { anchor: range.from + insert.length },
        };
      }));
    }
  }

  destroy() { this.view.destroy(); }
}

/** CodeMirror(container, opts) 相当 */
export function create(container, opts) {
  return new CmController(container, opts);
}

/** CodeMirror.fromTextArea(textarea, opts) 相当。textarea は隠して置き換える。 */
export function fromTextarea(textarea, opts) {
  const parent = document.createElement('div');
  parent.className = 'cm-host';
  textarea.style.display = 'none';
  textarea.parentNode.insertBefore(parent, textarea.nextSibling);
  const controller = new CmController(parent, { ...opts, value: textarea.value });
  controller.textarea = textarea;
  return controller;
}
