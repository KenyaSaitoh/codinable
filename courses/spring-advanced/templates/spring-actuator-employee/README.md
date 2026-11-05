# spring-actuator-employee — Spring Boot Actuator

社員管理 REST API（`/employees`・`/departments`）に Spring Boot Actuator を組み込み、稼働状態・情報・メトリクス・設定値を HTTP で観測します。外部サービスは不要です。

## DB について（Codinable での変更点）

講座では共有の HSQLDB サーバー（localhost:9001）を使います。Codinable では HSQLDB サーバーを使わず、**インメモリ DB**（`jdbc:hsqldb:mem:testdb`）で動かします。起動のたびに `src/main/resources/schema.sql`・`data.sql` が流れ、社員 16 件・部署 4 件から始まります（`application.yml` の `spring.datasource.url` と `spring.sql.init.mode` を変更してあります）。

`sql/hsqldb/` の SQL は講座の HSQLDB サーバー用の初期化スクリプトです。

## 動かし方

▶ アプリ実行で起動したまま（ポート 8080）、`curl/` のスクリプトを順に開いて、エディタの右上の ▶ 実行で送ります。
各スクリプトの先頭のコメントに、期待する結果を書いてあります。

| スクリプト | 確かめること |
|---|---|
| `01_call_api.sh` | 社員 API を呼んでメトリクスを発生させる |
| `02_health.sh`〜`03_info.sh` | `/actuator/health`・`/actuator/info` |
| `14_health_probes.sh` | `/actuator/health/liveness`・`/actuator/health/readiness` |
| `04_metrics.sh`〜`07_http_server_requests.sh` | メトリクス名の一覧、カスタムカウンター、JVM、HTTP リクエストの統計 |
| `08_env.sh`〜`09_env_property.sh` | 設定値の一覧と 1 項目 |
| `10_loggers.sh`〜`11_change_log_level.sh` | ログレベルの一覧と、再起動なしの変更 |
| `12_mappings.sh`〜`13_beans.sh` | リクエストマッピングと Bean の一覧 |

- `/actuator/health` … `components.database` にカスタムの `DatabaseHealthIndicator` の結果（DB 名と社員件数）が出ます
- `/actuator/health/liveness` … プロセスが動き続けられるか（`livenessState` だけで判定し、`{"status":"UP"}` を返す。DOWN なら再起動の対象）
- `/actuator/health/readiness` … 要求を受け付けられるか（`readinessState` と `database` で判定する。DOWN なら 503 を返し、振り分けの対象から外す）
- `/actuator/info` … `info.app.*` と `AppInfoContributor` の起動時刻・Java バージョン
- `/actuator/metrics` … `employee.get.count`（カスタムカウンター）、`http.server.requests`、`jvm.memory.used` など
- `/actuator/env`・`/actuator/loggers`・`/actuator/mappings`・`/actuator/beans` も公開しています

`05_employee_get_count.sh` を `01_call_api.sh` の前後で送ると、カウンターが増えるのを確かめられます。講座にある「HSQLDB サーバーを止めて DOWN（503）を再現する」手順は、インメモリ DB では行えません。

## 主なファイル

- `src/main/resources/application.yml` … 公開するエンドポイント、health の詳細表示、`management.endpoint.health.probes.enabled` と readiness のグループ
- `src/main/java/pro/kensait/spring/employee/rest/health/DatabaseHealthIndicator.java` … カスタムの HealthIndicator
- `src/main/java/pro/kensait/spring/employee/rest/service/EmployeeService.java` … `MeterRegistry` によるカスタムカウンター
