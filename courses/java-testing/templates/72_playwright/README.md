# 72_playwright: Playwright による UI テスト

## 何を学ぶ演習か

`71_selenide` と同じ操作を Playwright で書き、自動待機の検証、確認ダイアログの扱い、codegen とトレースビューアーを確かめます（チャプター7.2）。

## 見るファイル

- `src/test/java/pro/kensait/playwright/bookstore/BookStoreOptions.java`: 起動の設定（画面の表示、ベース URL）
- `src/test/java/pro/kensait/playwright/bookstore/BookStoreTest.java`: UI テスト（コメントの番号が原稿の操作の番号）
- `spring-bookstore-mvc/src/main/resources/templates/`: テスト対象の画面

## ボタンでの動かし方

1. アプリ実行を押します。`spring-bookstore-rest`（ポート 8081）と `spring-bookstore-mvc`（ポート 8080）が起動し、「起動しました」と出ます。アプリは出力欄の右の列で起動・停止できます。ログインは `alice@example.com` と `password` です。
2. 起動したまま、`BookStoreTest.java` を開いて、エディタの右上の ファイル実行を押します。初回は Web ブラウザ（Chromium、Firefox、WebKit）のダウンロードが終わってからテストが始まります。

`spring-bookstore-mvc` を書き換えたときと、在庫を初期状態に戻すときは、アプリ実行を押して起動し直します。

## 結果のどこを見るか

- 「テスト結果」の `test_BookStoreOperations()` の成否
- `trace/BookStoreTest.zip` のトレース。`scripts/02_show_trace.sh` を開いて ファイル実行を押すと、トレースビューアーで開きます
- `scripts/01_codegen.sh` を開いて ファイル実行を押すと codegen が起動し、操作がコードの下書きとして記録されます（閉じると終わります）
