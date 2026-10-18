# spring-observability-employee — Micrometer Observation と OpenTelemetry

社員管理 REST API に Micrometer Observation と OpenTelemetry を組み込み、メトリクス・トレース・ログを関連付けます。1 つの計装（Observation）からタイマーメトリクスとスパンの両方が生成されます。

- `Observation.createNotStarted("employee.get", ...)` … API を直接使う例（`getEmployee`）
- `@Observed(name = "employee.get-all")` … アノテーションで宣言する例（`getEmployeesAll`。`ObservationConfig` の `ObservedAspect` が計装）

## DB について（Codinable での変更点）

講座では共有の HSQLDB サーバー（localhost:9001）を使います。Codinable では HSQLDB サーバーを使わず、**インメモリ DB**（`jdbc:hsqldb:mem:testdb`）で動かします。起動のたびに `src/main/resources/schema.sql`・`data.sql`（`sql/hsqldb/` と同じ内容）が流れ、社員 16 件・部署 4 件から始まります。

## 動かし方

▶ アプリ実行で起動したまま（ポート 8080）、`curl/` のスクリプトを順に開いて、エディタの右上の ▶ 実行で送ります。
`01_get_employee.sh`・`02_get_employees.sh` で計装された API を呼び、`03_metrics.sh` 〜 `05_http_server_requests.sh` で
Observation から生成されたタイマー（`employee.get`・`employee.get-all`）と HTTP リクエストの統計を見ます。
各スクリプトの先頭のコメントに、期待する結果を書いてあります。

出力タブのログには `INFO [spring-observability-employee,<traceId>,<spanId>]` の形式で traceId／spanId が出ます。同じリクエストのログは traceId が同じで、spanId が処理単位で変わることを確かめます。

## トレースについて（Codinable での変更点）

講座では、トレースを OTLP で Jaeger へ送り、Webブラウザの画面でスパンの親子関係を確かめます。
Codinable では Jaeger を使わないため、`application.yml` の `management.tracing.export.otlp.enabled: false` で送信だけを止めています。
スパンは作られるので、ログの traceId／spanId で同じリクエストの処理をたどれます。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/rest/service/EmployeeService.java` … Observation API と `@Observed` による計装
- `src/main/java/pro/kensait/spring/employee/rest/observation/ObservationConfig.java` … `ObservedAspect` の登録
- `src/main/resources/application.yml` … サンプリング率、OTLP の送信先（Codinable では送信を止めている）、traceId／spanId を含むログ形式
