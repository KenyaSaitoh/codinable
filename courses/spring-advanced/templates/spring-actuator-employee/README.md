# spring-actuator-employee — Spring Boot Actuator

社員管理 REST API（`/employees`・`/departments`）に Spring Boot Actuator を組み込み、稼働状態・情報・メトリクス・設定値を HTTP で観測します。外部サービスは不要です。

## DB について（Codinable での変更点）

講座では共有の HSQLDB サーバー（localhost:9001）を使います。Codinable では HSQLDB サーバーを使わず、**インメモリ DB**（`jdbc:hsqldb:mem:testdb`）で動かします。起動のたびに `src/main/resources/schema.sql`・`data.sql` が流れ、社員 6 件・部署 3 件から始まります（`application.yml` の `spring.datasource.url` と `spring.sql.init.mode` を変更してあります）。

`sql/hsqldb/` の SQL は講座の HSQLDB サーバー用の初期化スクリプトです。

## 動かし方

実行対象 `gradle:bootRun` で「実行」を押し、ターミナルで確かめます（ポート 8080）。

```bash
curl -i http://localhost:8080/employees/1
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/actuator/info
curl -i http://localhost:8080/actuator/metrics/employee.get.count
```

- `/actuator/health` … `components.database` にカスタムの `DatabaseHealthIndicator` の結果（DB 名と社員件数）が出ます
- `/actuator/info` … `info.app.*` と `AppInfoContributor` の起動時刻・Java バージョン
- `/actuator/metrics` … `employee.get.count`（カスタムカウンター）、`http.server.requests`、`jvm.memory.used` など
- `/actuator/env`・`/actuator/loggers`・`/actuator/mappings`・`/actuator/beans` も公開しています

ログレベルを再起動なしで変更する例:

```bash
curl -i -X POST http://localhost:8080/actuator/loggers/pro.kensait.spring.employee \
  -H 'Content-Type: application/json' -d '{"configuredLevel":"DEBUG"}'
```

その他の確認手順は `curlメモ.txt` にあります（Windows のコマンドプロンプト向けの書き方です。bash では `-d` の JSON を単一引用符で囲みます）。講座にある「HSQLDB サーバーを止めて DOWN（503）を再現する」手順は、インメモリ DB では行えません。

## 主なファイル

- `src/main/resources/application.yml` … 公開するエンドポイントと health の詳細表示
- `src/main/java/pro/kensait/spring/employee/rest/health/DatabaseHealthIndicator.java` … カスタムの HealthIndicator
- `src/main/java/pro/kensait/spring/employee/rest/service/EmployeeService.java` … `MeterRegistry` によるカスタムカウンター
