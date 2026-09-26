# spring-mvc-calc-4-prg — PRG とフラッシュ属性

チャプター 5.1「リダイレクトとセッションによる状態管理」（5.1.2）で使うサンプルです。
入力エラーを同じ画面に再表示する `CalcInputPage.html` の書き方は、チャプター 4.1（4.1.3）でも取り上げます。

- POST `/add` は計算結果をフラッシュ属性に入れて `redirect:/viewAddResult` を返す
- GET `/viewAddResult` がその値を Model の `value` に入れて `CalcOutputPage` を表示する

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開き、足し算を実行する
3. URL が `/viewAddResult` に変わっていること、再読み込みしても POST が再送されないことを確かめる
   （フラッシュ属性は 1 回きりなので、再読み込み後は値が消える）

`logback-spring.xml` の設定により、ログはコンソールに加えてプロジェクト直下の
`spring_mvc_calc_4.log` にも書き出されます。
