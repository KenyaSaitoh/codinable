# spring-mvc-calc-2-validation — データバインディングと Bean Validation

チャプター 4.1「Spring MVCのリクエスト処理・入力検証・例外処理」で使うサンプルです。
入力は `CalcParam`（`@NotNull` と -1000〜1000 の範囲制約）にバインドされ、
`CalcController` が `BindingResult` を見て入力画面へ戻すか計算するかを決めます。
計算は `@Service` の `CalcService` に任せ、Controller にはフィールドインジェクションで注入しています
（チャプター 2.1 で取り上げる例です）。

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. プレビューで `http://localhost:8080/` を開く
3. 空欄・文字・範囲外（例: 2000）を入れて送信し、入力画面にエラーが出ることを確かめる

エラーメッセージの文言は `ValidationMessages.properties` にあります。

## 主なファイル

- `src/main/java/pro/kensait/spring/calc/web/CalcParam.java` … 入力値と検証の制約
- `src/main/java/pro/kensait/spring/calc/web/CalcController.java` … BindingResult による分岐
- `src/main/java/pro/kensait/spring/calc/service/CalcService.java` … 計算
- `src/main/resources/ValidationMessages.properties` … エラーメッセージ
