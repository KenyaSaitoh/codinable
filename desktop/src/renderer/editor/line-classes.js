// ═══════════════════════════════════════════════════════════
//  行への CSS クラス付与 (CodeMirror 6)
//
//  CM5 の addLineClass / removeLineClass(line, 'background', cls) の置き換え
//  カバレッジ表示 (cm-cov-full / -part / -miss) と講義のステップ強調
//  (cm-lecture-line) が使う。クラス名は CM5 時代のまま維持しているので、
//  style.css の配色定義はそのまま効く
//
//  行番号は呼び出し側 (renderer.js) に合わせて **0 始まり**で受ける
// ═══════════════════════════════════════════════════════════

import { StateField, StateEffect } from '@codemirror/state';
import { Decoration, EditorView } from '@codemirror/view';

export const addLineClassEffect    = StateEffect.define();   // { line, cls }
export const removeLineClassEffect = StateEffect.define();   // { line, cls }

const lineDeco = new Map();
function decoFor(cls) {
  if (!lineDeco.has(cls)) lineDeco.set(cls, Decoration.line({ class: cls }));
  return lineDeco.get(cls);
}

/** 付与中の { line(0始まり) → Set<class> } を保持し、そこから DecorationSet を組む */
export const lineClassField = StateField.define({
  create: () => ({ map: new Map(), decorations: Decoration.none }),

  update(value, tr) {
    let map = value.map;
    let changed = false;

    for (const effect of tr.effects) {
      if (effect.is(addLineClassEffect)) {
        if (!changed) { map = new Map([...map].map(([k, v]) => [k, new Set(v)])); changed = true; }
        const set = map.get(effect.value.line) || new Set();
        set.add(effect.value.cls);
        map.set(effect.value.line, set);
      } else if (effect.is(removeLineClassEffect)) {
        const set = map.get(effect.value.line);
        if (!set || !set.has(effect.value.cls)) continue;
        if (!changed) { map = new Map([...map].map(([k, v]) => [k, new Set(v)])); changed = true; }
        const next = map.get(effect.value.line);
        next.delete(effect.value.cls);
        if (next.size === 0) map.delete(effect.value.line);
      }
    }

    // 行数が変わると 0 始まり行番号の指す先がずれるため、範囲外は落として組み直す
    if (!changed && !tr.docChanged) return value;
    return { map, decorations: build(map, tr.state) };
  },

  provide: f => EditorView.decorations.from(f, v => v.decorations),
});

function build(map, state) {
  if (map.size === 0) return Decoration.none;
  const ranges = [];
  for (const line of [...map.keys()].sort((a, b) => a - b)) {
    if (line < 0 || line >= state.doc.lines) continue;
    const from = state.doc.line(line + 1).from;
    for (const cls of map.get(line)) ranges.push(decoFor(cls).range(from));
  }
  return Decoration.set(ranges, true);
}
