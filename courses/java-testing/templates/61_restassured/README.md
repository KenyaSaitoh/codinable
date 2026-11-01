# 61_restassured: RestAssured による REST API のテスト

## 何を学ぶ演習か

起動した REST API に HTTP リクエストを送り、Given-When-Then の形でレスポンスを検証します（チャプター6.1）。

## 見るファイル

- `src/test/java/pro/kensait/spring/calc/rest/test/CalcApiTest.java`: 計算 API のテスト
- `src/test/java/pro/kensait/spring/person/rest/test/PersonApiTest.java`: Person の REST API のテスト
- `spring-rest-calc/`、`spring-rest-person/`: テスト対象の Spring Boot アプリケーション（`spring-rest-calc` は 8080、`spring-rest-person` は 8082）
- `spring-rest-person/src/main/java/pro/kensait/spring/person/rest/api/PersonApi.java`: 誤りを入れて確かめる場所

## ボタンでの動かし方

1. ▶ アプリ実行を押します。`spring-rest-calc` と `spring-rest-person` が起動し、「起動しました」と出ます。アプリは出力欄の右の列で起動・停止できます。
2. 起動したまま、テストクラスを開いてエディタの右上の ▶ 実行を押します。
3. `scripts/01_person_curl.sh` を開いて ▶ 実行を押すと、本物の Person API のレスポンスを見られます（6.2 のモックとの比較用）。

`spring-rest-person` を書き換えたときと、データを初期状態に戻すときは、▶ アプリ実行を押して起動し直します。

## 結果のどこを見るか

- 「テスト結果」のメソッドごとの成否
- 同じテストを続けて流しても成功するか（サーバーのデータはテストの後も残ります）
- `test_CreatePerson()` と `test_DeletePerson()` がステータスコードしか確かめていないこと
