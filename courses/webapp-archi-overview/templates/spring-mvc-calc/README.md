# spring-mvc-calc — Spring MVC の計算アプリケーション

チャプター 8.2「Spring MVCによるアプリケーション構築」の演習です。
Controller + Thymeleaf で作った四則演算の計算アプリケーションで、入力値検証・0 除算のエラー・
計算履歴を備えています。同じものを Django 版（django-calc）・Express 版（express-calc）でも作っています。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `src/main/java/pro/kensait/spring/calc/CalcController.java` | 主役。`GET /` で入力画面、`POST /add` などで計算、`GET /history` で履歴 |
| `src/main/java/pro/kensait/spring/calc/CalcParam.java` | フォームの値。`@NotNull` / `@Min(-1000)` / `@Max(1000)` で入力値を検証する |
| `src/main/java/pro/kensait/spring/calc/CalcService.java` | 計算と履歴（画面から切り離したロジック） |
| `src/main/resources/templates/*.html` | Thymeleaf の画面（入力・結果・履歴） |
| `src/test/java/pro/kensait/spring/calc/CalcServiceTest.java` | CalcService の単体テスト（Spring を起動しない） |

## 動かし方

1. ▶ アプリ実行を押す。`http://localhost:8080` で起動し、プレビュータブが自動で開く
   （初回は Gradle の取得に数分かかる）
2. 計算する。空欄や範囲外の値、0 での割り算も試す
3. 「計算履歴を見る」を開く。`/history?limit=5` のように件数も指定できる（省略時は 10）
4. 終わったら「停止」で止める

テストは `CalcServiceTest.java` を開いて、エディタの右上の ▶ 実行で流します。結果はテスト結果タブに出ます。

## 見どころ

- 計算は POST、履歴の表示は GET。HTTP メソッドの使い分け
- 検証エラーのときは同じ入力画面を返し、メッセージを表示している
