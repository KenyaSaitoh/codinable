# javascript-basics — JavaScript と TypeScript を動かす

チャプター 7.1〜7.2「JavaScriptの基本 / 発展とTypeScript」の演習です。
Node.js でそのまま動くスクリプトを 3 本用意してあり、ブラウザーを使わずに言語の書き方を確かめます。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `01_basics.js` | const / let、テンプレートリテラル、アロー関数、map / filter / reduce / find、分割代入とスプレッド構文 |
| `02_async.js` | Promise、async / await、fetch（`https://httpbin.org/json` を取得する） |
| `03_types.ts` | 型エイリアスとインターフェース、引数と戻り値の型、ユニオン型の絞り込み、ジェネリクス |

## 動かし方

1. `01_basics.js` を開き、エディタの右上の ▶ 実行を押す
2. `console.log` の出力が「実行結果」に出る。`02_async.js`・`03_types.ts` も順に開いて ▶ 実行で動かす

`03_types.ts` もビルドせずにそのまま動きます。Node.js 24 は実行時に型注釈だけを取り除く
（type stripping）ため、型は「実行前に間違いを見つけるための注釈」で、実行時には消えます。
`02_async.js` はインターネットに接続して fetch します。

## 見どころ

- `02_async.js` で、`await` で待った処理と待たない処理の出力の順番
- 値を書き換えて ▶ 実行し直し、出力がどう変わるか
