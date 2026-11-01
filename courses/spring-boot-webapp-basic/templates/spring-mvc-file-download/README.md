# spring-mvc-file-download — Resource と ResponseEntity によるダウンロード

チャプター 6.1「Spring MVCによるファイルアップロードとダウンロード」（6.1.5）で使うサンプルです。
`FileDownloadController` が `C:/tmp/output` の下のファイルを `Resource` として読み、
`Content-Disposition: attachment` を付けた `ResponseEntity` で返します。

## 動かし方

1. `scripts/01_prepare_sample.sh` を開き、エディタの右上の ▶ 実行で
   ダウンロードするファイル `C:/tmp/output/sample.txt` を用意する
2. ツールバーの ▶ アプリ実行で起動する
3. `scripts/02_download.sh` を開いて ▶ 実行し、`HTTP/1.1 200` と
   `Content-Disposition: attachment; filename="sample.txt"`、本文 `download sample` を確かめる
4. Web ブラウザで `http://localhost:8080/download/sample.txt` を開くと、ファイルとして保存される
   （存在しない名前を指定すると例外になる）

`http://localhost:8080/` はファイル名の入力画面です。
保存場所 `C:/tmp/output` はサンプルのコードに直接書かれています。

## 主なファイル

- `src/main/java/pro/kensait/spring/download/FileDownloadController.java` … `/download/{fileName}` で `Resource` を返す
- `src/main/resources/templates/FileDownloadPage.html` … ファイル名の入力画面
- `scripts/01_prepare_sample.sh` … ダウンロードするファイルを用意する
- `scripts/02_download.sh` … curl でダウンロードして応答ヘッダーを見る
