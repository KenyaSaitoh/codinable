# spring-mvc-file-download — Resource と ResponseEntity によるダウンロード

チャプター 6.1「Spring MVCによるファイルアップロードとダウンロード」（6.1.5）で使うサンプルです。
`FileDownloadController` が `C:/tmp/output` の下のファイルを `Resource` として読み、
`Content-Disposition: attachment` を付けた `ResponseEntity` で返します。

## 動かし方

1. ダウンロードするファイルを用意する。ターミナルで次を実行する

   ```bash
   mkdir -p /c/tmp/output
   echo "download sample" > /c/tmp/output/sample.txt
   ```

2. 実行対象で `gradle:bootRun` を選び、「実行」を押す
3. Web ブラウザで `http://localhost:8080/download/sample.txt` を開き、ファイルとして保存されることを確かめる
   （存在しない名前を指定すると例外になる）

`http://localhost:8080/` はファイル名の入力画面です。
保存場所 `C:/tmp/output` はサンプルのコードに直接書かれています。
