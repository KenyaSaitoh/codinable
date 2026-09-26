# spring-mvc-calc-2-validation — データバインディングと Bean Validation

チャプター 4.1「Spring MVCのリクエスト処理・入力検証・例外処理」で使うサンプルです。
入力は `CalcParam`（`@NotNull` と -1000〜1000 の範囲制約）にバインドされ、
`CalcController` が `BindingResult` を見て入力画面へ戻すか計算するかを決めます。
計算は `@Service` の `CalcService` に任せ、Controller にはフィールドインジェクションで注入しています
（チャプター 2.1 で取り上げる例です）。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開く
3. 空欄・文字・範囲外（例: 2000）を入れて送信し、入力画面にエラーが出ることを確かめる

エラーメッセージの文言は `ValidationMessages.properties` にあります。
