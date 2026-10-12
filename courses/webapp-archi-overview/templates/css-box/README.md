# css-box — ボックスモデルで余白を制御する

チャプター 3.3「CSSによるWebページ修飾」の演習です。
すべての要素は content / padding / border / margin の 4 つの領域からなる箱として配置されます。
その入れ子の様子と、`box-sizing`・`margin`・`display`・`overflow` の振る舞いの違いを並べてあります。

## 主なファイル

- `index.html` … 5 つの見本（4 つの領域、width の範囲、margin の指定、display、overflow）
- `style.css` … 見本ごとに区切りコメントを付けてあります

## 動かし方

1. ▶ アプリ実行を押すと内蔵の静的サーバーが起動し、出力欄の「🌐 プレビュー」タブにページが開きます
2. `style.css` の数値（`padding`・`border`・`margin`・`width` など）を変えて保存し、
   プレビューを再読み込みして箱の大きさや位置の変化を見ます

## 見どころ

- 4 つの領域: 青い枠が border、水色が padding、白が content
- 同じ `width` でも、既定の `box-sizing: content-box` では padding と border が外側に足され、
  `border-box` では padding と border を含めた全体がその幅になる
- `margin` の書き方: 1 つの値（四方）、2 つの値（上下・左右）、左右を `auto` にして中央に寄せる
- `display` の違い: `inline` では width / height が効かない、`block` は横幅いっぱいに広がる、
  `inline-block` なら横に並びつつ幅が効く、`none` は表示されない
- 中身がはみ出したときの `overflow`: `visible`（はみ出して見える）、`hidden`（切り取る）、`auto`（スクロールバーを出す）
