# spring-mvc-file-upload — マルチパートと MultipartFile

チャプター 6.1「Spring MVCによるファイルアップロードとダウンロード」の前半で使うサンプルです。

- `FileUploadPage.html` のフォームが `multipart/form-data` でタイトルとファイルを送る
- `FileUploadController` が `MultipartFile` で受け取り、`FileStorageService` に保存を任せる
- 保存先の名前はアプリケーションが発行するキーで、元のファイル名はメタデータとして持つ

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開き、タイトルとファイルを選んで保存する
3. 結果画面の `/files/{id}` で保存した内容を確かめる

保存先は OS の一時フォルダの下の `spring-file-upload` です。
