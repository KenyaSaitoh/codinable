# 81_gatling: Gatling による負荷テスト

## 何を学ぶ演習か

テックブックストアにシナリオで負荷をかけ、結果のサマリーとレポートをパーセンタイルで読みます（チャプター8.1）。

## 見るファイル

- `src/gatling/java/pro/kensait/gatling/bookstore/scenario1/BookStoreSimulation.java`: シミュレーション
- `src/gatling/resources/data/users.csv`: フィーダーのデータ
- `scripts/01_gatling_run.sh`: `gatlingRun` を流すスクリプト
- `build.gradle`: `io.gatling.gradle` プラグインの設定

## ボタンでの動かし方

1. ▶ アプリ実行を押します。`spring-bookstore-rest`（ポート 8081）と `spring-bookstore-mvc`（ポート 8080）が起動し、「起動しました」と出ます。アプリは出力欄の右の列で起動・停止できます。ログインは `alice@example.com` と `password` です。
2. 起動したまま、`scripts/01_gatling_run.sh` を開いて、エディタの右上の ▶ 実行を押します。約 3 分 20 秒かかります。

`spring-bookstore-mvc` を書き換えたときと、在庫を初期状態に戻すときは、▶ アプリ実行を押して起動し直します。

## 結果のどこを見るか

- 実行結果のサマリーの、リクエスト数と OK / KO、最小・平均・最大、50・75・95・99 パーセンタイル
- HTML のレポート `build/reports/gatling/bookstoresimulation-<日時>/index.html`
- `assertions()` を加えて基準を満たさないとき、`gatlingRun` が失敗として終わること
