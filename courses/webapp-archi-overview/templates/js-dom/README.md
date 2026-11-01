# js-dom — DOM を操作して書き換える

チャプター 3.2「JavaScriptによるWebページ振る舞い制御」の最初の演習です。
HTML に書いた内容を、JavaScript があとから書き換えます。
`index.html` には「書き換え前」の文字が入っているので、ソースとプレビューを見比べると
JavaScript が何をしたかが分かります。

## 主なファイル

- `index.html` … 書き換え前の HTML。行を入れる空の `tbody`（`person-list`）と記録欄（`log`）があります
- `app.js` … DOM 操作の本体。番号付きのコメント（1〜6）の順に読みます
- `style.css` … 見た目。`.highlighted` クラスの装飾もここにあります

## 動かし方

1. ▶ アプリ実行を押すと内蔵の静的サーバーが起動し、出力欄の「🌐 プレビュー」タブにページが開きます
2. 画面下の「やったこと」に、`app.js` が行った操作が順に並びます
3. `app.js` を書き換えて保存し、プレビューを再読み込みして結果を見ます
   （書き換わるのはメモリ上の DOM ツリーで、`index.html` は変わりません）

## 見どころ

- 要素を取り出す: `getElementById`（id で 1 つ）、`querySelector`（CSS セレクタで最初の 1 つ）、
  `querySelectorAll`（一致したすべて）
- `textContent` はタグも文字として入る。`innerHTML` は HTML として解釈されるので、
  外部から来た文字（入力や API の応答）には使わない（XSS の入口になる）
- `style` で直接色を付ける方法と、`classList.add` でクラスを付け外しして見た目は CSS に任せる方法
- `createElement` と `appendChild` / `append` で表の行を組み立てる
- `setAttribute` で属性（ここではツールチップの `title`）を付ける
- `script` を `body` の末尾に置く理由（DOM が組み立て終わってから実行されるため）
