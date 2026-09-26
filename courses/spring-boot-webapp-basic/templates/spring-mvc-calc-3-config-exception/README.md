# spring-mvc-calc-3-config-exception — 外部設定と業務例外

チャプター 4.1「Spring MVCのリクエスト処理・入力検証・例外処理」の後半（4.1.4・4.1.5）で使うサンプルです。

- `ConfigHolder` が `config.properties` の `calc.result.scale`（桁数 4）と `calc.result.limit`（上限 100000）を `@Value` で受け取る
- `CalcService` が `BigDecimal` で計算し、上限超過で `LimitOverException`、ゼロ除算で `ZeroDivideException` を送出する
- `CalcController` が例外を受け取り、`messages.properties` の文言で `CalcErrorPage` を表示する

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開く
3. 掛け算で 1000 × 1000（上限超過）、割り算で 0 除算を試し、エラー画面へ分岐することを確かめる
4. 割り算で 1 ÷ 3 を試し、小数第 4 位で丸められることを確かめる
