# gatling-employee：Gatling による負荷と応答時間の検証（チャプター7.1）

社員管理 Web アプリに Gatling で HTTP 負荷をかけ、応答内容（checks）と
件数・失敗・p95（assertions）を検証します。

## シナリオ

`src/gatling/java/pro/kensait/spring/employee/load/EmployeeSimulation.java` は
`rampUsers(20).during(10秒)` で合計20人を到着させ、各ユーザーが次の3リクエストを1回ずつ送ります（合計60件）。

| リクエスト | 応答の check |
|---|---|
| 社員一覧画面 | 200、見出し、総件数10、先頭5人の氏名と順序 |
| 部署別社員検索 | 200、選択した部署、該当社員の氏名と順序 |
| 社員編集フォーム | 200、更新画面、送信先、氏名・部署・役職・月給・入社日・version |

入力と期待値は `src/gatling/resources/employees.csv` にあります。
assertions は「全60件・各リクエスト20件」「失敗0件」「全体 p95 < 1000ms」です。

## Codinable での動かし方

Codinable の実行対象には `gatlingRun` が出ないため、負荷を受けるアプリと Gatling を分けて動かします。

1. 実行対象 `bootRun`（既定）で「実行」を押し、アプリを <http://localhost:8080> で起動する
   （プレビューで `/employees` が開けば準備完了）
2. アプリを起動したまま、ターミナルで Gatling を実行する

```bash
curl http://localhost:8080/actuator/health
./gradlew.bat gatlingRun
```

接続先の既定は `http://localhost:8080` です（環境変数 `GATLING_BASE_URL` で変えられます）。
結果はターミナルに集計が出るほか、`build/reports/gatling/employeesimulation-<実行日時>/index.html` に
HTML レポートができます。assertions を満たさないと Gradle も失敗で終わります。

画面から初期社員を変更・削除した場合は、アプリを止めて起動し直してから測ります
（H2 はインメモリなので、起動し直すと CSV に対応する初期データに戻ります）。

## テスト

実行対象 `test` では、負荷をかける前の確認として `EmployeeApplicationSmokeTest` を実行します。
アプリと H2 をテストが自動で起動し、一覧 HTML・health と、CSV の3行が初期データと対応していることを確かめます。
元のサンプルではこのテスト（`@Tag("it")`）を `integrationTest` で流していました。
Codinable の「test」で走るよう、除外タグを外しています（`build.gradle` のコメント参照）。

JDK 25 で Gatling を動かすため、`build.gradle` の `gatling.jvmArgs` に
`--add-opens=java.base/java.lang=ALL-UNNAMED` などを指定しています。
