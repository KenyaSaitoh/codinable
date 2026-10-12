# html-form — フォームで入力を受け取る

チャプター 3.1「HTMLによるWeb構造定義」の演習です。
社員登録フォームに、`input` の type 一式と `required` や `pattern` などの検証属性を並べてあります。
入力欄の種類と検証はすべて HTML の機能で、JavaScript は送信後の URL を表示する 1 か所だけです。

## 主なファイル

- `index.html` … フォーム本体と、送信された値（URL のクエリパラメータ）を表示する部分
- `form.css` … 入力欄を縦に並べて読みやすくするだけの CSS。読み流してかまいません

## 動かし方

1. ▶ アプリ実行を押すと内蔵の静的サーバーが起動し、出力欄の「🌐 プレビュー」タブにページが開きます
2. 次の順に試します
   - 何も入力せずに「送信」を押す → Web ブラウザが必須入力を弾く
   - メール欄に `abc` と入れて送信 → 形式のエラーが出る
   - 正しく入力して送信 → ページが再読み込みされ、「送信された値」と URL に入力値が並ぶ

## 見どころ

- `form` の `action`（送信先）と `method`（HTTP メソッド）。ここでは `action=""`・`method="get"` なので、
  入力値が URL のクエリパラメータとして見える（見せたくない値は POST を使う。4.1 で扱います）
- `label` の `for` と `input` の `id` を一致させると、ラベルを押して入力欄に移れる
- type の種類: `text` / `email` / `tel` / `number` / `date` / `time` / `url` / `range` / `color`、
  `select`・ラジオボタン・チェックボックス（`fieldset` と `legend` でまとめる）・`textarea`
- 検証属性: `required`・`minlength` / `maxlength`・`min` / `max` / `step`・`pattern`（正規表現）と `title`
- `readonly` は変更できないが送信される、`disabled` は送信もされない、`hidden` は画面に出さず値だけ送る
- チェックボックスは `name` をそろえると、同じ項目の複数値として送られる
