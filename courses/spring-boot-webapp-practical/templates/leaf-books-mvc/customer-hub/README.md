# Customer Hub（外部 API 役）

書店のログインと顧客登録で呼ばれる顧客 API です（ポート 8081）。
親フォルダの書店とは別の Gradle プロジェクトなので、ターミナルから起動します。

```bash
cd customer-hub
../gradlew.bat bootRun
```

`../gradlew.bat` は、親フォルダで一度「実行」を押すと Codinable が用意します。
まだ無いときは、先に親フォルダの実行ボタンを押してから起動してください。

止めるときはターミナルで Ctrl+C を押します。
DB はインメモリで、起動のたびに `sql/hsqldb/` の内容から作り直されます。

確認: `http://localhost:8081/customers/1`
