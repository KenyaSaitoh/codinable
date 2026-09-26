# spring-mvc-calc-5-session — @SessionAttributes による複数画面の状態管理

チャプター 5.1「リダイレクトとセッションによる状態管理」（5.1.4）で使うサンプルです。
計算結果を `CalcSession` としてセッションに保持し、次の画面の `/calcTax` で
`TaxService` が税込金額（1.1 倍）を求めます。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開き、計算する
3. 結果画面から税込計算へ進み、前の画面の結果が引き継がれていることを確かめる
