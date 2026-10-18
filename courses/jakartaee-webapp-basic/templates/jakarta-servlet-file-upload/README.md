# jakarta-servlet-file-upload — マルチパートと @MultipartConfig・Part

チャプター 8.1「ファイルのアップロードとダウンロード」のうち、サーブレットでファイルを受け取るサンプルです。
`multipart/form-data` のフォームを `@MultipartConfig` を付けた `FileUploadServlet` が受け、`getPart` で取り出した `Part` を
`FileStorageService` に渡して検証・保存し、結果の表示は `/files/result?id=…` の GET へリダイレクトで分けます（PRG）。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/upload/` の下にあります。

| ファイル | 内容 |
|---|---|
| `src/main/webapp/FileUploadPage.html` | `enctype="multipart/form-data"` のフォーム |
| `FileUploadServlet.java` | `@MultipartConfig` の上限（1 ファイル 5MB・全体 10MB）と、上限超過の 413 |
| `FileStorageService.java` | ファイル名・容量・形式の検証と、`UUID` の保存先キーでの保存。メタデータはメモリー上の Map |
| `StoredFile.java`、`InvalidFileException.java` | 保存したファイルのメタデータと、受け付けないファイルを表す例外 |
| `UploadResultServlet.java`、`src/main/webapp/WEB-INF/views/uploadResult.jsp` | ID で保存済みの情報を取り出して表示する |
| `src/main/webapp/WEB-INF/web.xml` | `InvalidFileException` を理由の画面（`uploadError.jsp`）に対応付ける `error-page` |
| `curl/01_upload.sh` 〜 `curl/03_too_large.sh` | 正常・不正な形式と空のファイル・上限超過を送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-servlet-file-upload/` のアップロード画面が開く
2. タイトルを入れ、ファイルを選んで「保存」を押す
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で 1 つずつ流す

## 結果のどこを見るか

- 保存すると、アドレスは `/files/result?id=1` に変わる。再読み込みしても保存は繰り返されない
- ファイル本体は OS の一時ディレクトリの下の `jakarta-file-upload` に、元の名前ではなく `UUID` の名前で置かれる
  （元のファイル名は表示用のメタデータとしてだけ持つ）
- `01_upload.sh` はファイル名を `fakepath/memo.txt` としてフォルダー付きで送るが、結果画面のファイル名は `memo.txt` になる
- `02_invalid_file.sh`: `.exe` や空のファイルは保存されず、理由が表示される
- `03_too_large.sh`: 5MB を超えるファイルは 413 になる
- メタデータはメモリー上にあるので、▶ アプリ実行でデプロイし直すと ID は 1 から振り直される
