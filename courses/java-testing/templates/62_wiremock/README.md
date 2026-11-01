# 62_wiremock: WireMock によるモックサーバーの構築

## 何を学ぶ演習か

他システムの代わりに応答するモックサーバーを WireMock で作り、スタブの条件と優先順位、本物との食い違いを確かめます（チャプター6.2）。

## 見るファイル

- `src/main/java/pro/kensait/spring/calc/rest/test1/WireMockApp.java`: 基本のスタブ
- `src/main/java/pro/kensait/spring/calc/rest/test2/WireMockApp.java`: 条件と優先順位
- `src/main/java/pro/kensait/spring/person/rest/test1/WireMockApp.java`: Person のスタブ
- `src/main/java/pro/kensait/spring/person/rest/test2/WireMockApp.java`、`src/test/resources/__files/persons.json`: ファイルから返すスタブ
- `curl/`: モックサーバーに送るリクエスト

## ボタンでの動かし方

1. ▶ アプリ実行を押すと、そのレッスンの `WireMockApp` が起動します。別の `WireMockApp` は、開いてエディタの右上の ▶ 実行で起動します。
2. 起動したまま、`curl/` のスクリプトを開いて ▶ 実行を押します。スクリプトの先頭に、どの `WireMockApp` 向けかを書いてあります。
3. モックサーバーは待ち受けを続けます。止めるときは出力欄の「停止」を押します。
4. `scripts/01_person_api_test.sh` を開いて ▶ 実行を押すと、チャプター6.1 の `PersonApiTest` の `test_GetPerson()` を、起動中の `person.rest.test1.WireMockApp` に向けて流します（テストのコードは `src/personApiTest/java` にあります）。

## 結果のどこを見るか

- 実行結果の `HTTP/1.1 200` などのステータスとボディ
- 条件に一致しないときに WireMock が返す 404 の説明（どの条件が合わなかったか）
- `person.rest.test1` では、どの personId でも `alice` が返ること
