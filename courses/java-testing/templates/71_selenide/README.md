# 71_selenide: Selenium / Selenide による UI テスト

## 何を学ぶ演習か

テックブックストアのログインから注文までを Selenide で操作し、待機・ロケーター・検証の不足を読み取ります（チャプター7.1）。

## 見るファイル

- `src/test/java/pro/kensait/selenium/bookstore/BookStoreTest.java`: UI テスト（コメントの番号が原稿の操作の番号）
- `spring-bookstore-mvc/src/main/resources/templates/`: テスト対象の画面（`BookSelectPage.html`、`CartViewPage.html`、`BookOrderPage.html`、`OrderSuccessPage.html` など）
- `spring-bookstore-rest/`: 顧客情報の REST API

## ボタンでの動かし方

1. アプリ実行を押します。`spring-bookstore-rest`（ポート 8081）と `spring-bookstore-mvc`（ポート 8080）が起動し、「起動しました」と出ます。アプリは出力欄の右の列で起動・停止できます。ログインは `alice@example.com` と `password` です。
2. 起動したまま、`BookStoreTest.java` を開いて、エディタの右上の ファイル実行を押します。Chrome が起動し、画面が自動で操作されます。ドライバーは Selenium Manager が初回に用意します（Chrome が無い PC では Chrome for Testing もダウンロードします）。

`spring-bookstore-mvc` を書き換えたときと、在庫を初期状態に戻すときは、アプリ実行を押して起動し直します。

## 結果のどこを見るか

- 「テスト結果」の `test_BookStoreOperations()` の成否
- `screenshot/BookStoreTest/<実行日時>/` のスクリーンショット
- 失敗したときのスタックトレースと、自動で保存されたスクリーンショットのファイル名
