# jakarta-faces-file-upload — Faces の h:inputFile と保存処理の検証

チャプター 8.1「ファイルのアップロードとダウンロード」のうち、Jakarta Faces でファイルを受け取るサンプルです。
`h:inputFile` の値が `Part` としてバッキングBean に設定され、保存は `jakarta-servlet-file-upload` と同じ
`FileStorageService` が検証して行います。受け付けない理由は、入力欄のメッセージとして同じ画面に表示します。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/upload/` の下にあります。

| ファイル | 内容 |
|---|---|
| `src/main/webapp/FileUploadPage.xhtml` | `h:form` の `enctype="multipart/form-data"` と、`accept` を付けた `h:inputFile` |
| `FileUploadBean.java` | `Part` 型のプロパティ `file`。`InvalidFileException` を `uploadForm:file` のメッセージにする |
| `FileStorageService.java`、`StoredFile.java`、`InvalidFileException.java` | 検証と保存（サーブレット版と同じ） |
| `src/main/webapp/UploadResultPage.xhtml`、`UploadResultBean.java` | `f:viewParam` の ID で保存済みの情報を表示する |
| `src/main/webapp/WEB-INF/web.xml` | `FacesServlet` の `<multipart-config>` で容量の上限を決める |
| `curl/01_upload.sh` | 正常なファイルと、受け付けない形式のファイルを送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-file-upload/FileUploadPage.xhtml` が開く
2. タイトルを入れ、ファイルを選んで「保存」を押す。何も選ばずに押す、空のファイルを選ぶ、なども試す
3. `curl/01_upload.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

- 保存すると `UploadResultPage.xhtml?id=1` へリダイレクトする
- `accept` は Webブラウザのファイル選択欄を絞るだけ。`curl/01_upload.sh` のように `.exe` を直接送ると、
  サーバー側の `FileStorageService` が拒否し、ファイル欄の横に「この形式のファイルは保存できません」が出る
- タイトル・ファイルを空のまま送ると、`required="true"` のエラーになり `upload` は呼ばれない
