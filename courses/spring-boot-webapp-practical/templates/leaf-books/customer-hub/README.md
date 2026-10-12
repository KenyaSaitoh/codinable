# Customer Hub（外部 API 役）

書店のログインと顧客登録で呼ばれる顧客 API です（ポート 8081）。
親フォルダの書店とは別の Gradle プロジェクトですが、親フォルダで ▶ アプリ実行を押すと
書店と一緒に起動します（親フォルダの `codinable.services.json`。出力には `[customer-hub]` が付きます）。

止める・起動し直すときは、出力タブの行の右端の「customer-hub」のボタンを使います。
DB はインメモリで、起動のたびに `sql/hsqldb/` の内容から作り直されます。

確認: `http://localhost:8081/customers/1`
