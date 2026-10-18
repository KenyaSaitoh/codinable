# jakarta-servlet-file-download — HTTP レスポンスとしてのダウンロード

チャプター 8.1「ファイルのアップロードとダウンロード」のうち、サーブレットからファイル本体を返すサンプルです。
`FileDownloadServlet` は `/download/<ファイル名>` の要求で `C:/tmp/output` の下のファイルを探し、
`Content-Type`・`Content-Length`・`Content-Disposition` を本文より前に決めてから、ストリームで本体を書き出します。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/download/FileDownloadServlet.java` | `getPathInfo` でファイル名を取り出し、`normalize` と `startsWith` で保存領域の外を拒否する。`contentDisposition` は `filename` と `filename*` を付ける |
| `src/main/webapp/index.html` | ダウンロードのリンクを並べた画面 |
| `curl/01_prepare_sample.sh` | `C:/tmp/output` にダウンロードするファイルを用意する |
| `curl/02_download.sh`、`curl/03_not_found.sh` | 応答ヘッダーと、存在しない・保存領域の外を指す要求を確かめる |

## 動かし方

1. `curl/01_prepare_sample.sh` を開いて、エディタの右上の ▶ 実行で流す（`C:/tmp/output` にファイルができる）
2. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-servlet-file-download/` が開く
3. 画面のリンクからダウンロードする。`curl/02_download.sh`・`curl/03_not_found.sh` を ▶ 実行で流す

## 結果のどこを見るか

- `02_download.sh` の応答ヘッダーに `Content-Disposition: attachment; filename="sample.txt"; filename*=UTF-8''sample.txt` が出る
- 日本語の名前のファイルは、`filename` に ASCII の代替名、`filename*` に UTF-8 でエンコードした名前が入る
- 存在しないファイルや、`../` で保存領域の外を指す要求は、本文を送り始める前に `sendError` で 404 を返す（バックスラッシュを含む要求は Payara が先に 400 で拒否する）
