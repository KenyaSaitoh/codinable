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

## トレースを見る（Jaeger、任意）

**Jaeger は Codinable に同梱していません。** トレースの送信先（OTLP、既定 `http://localhost:4318/v1/traces`、環境変数 `OTLP_ENDPOINT` で変更可）が無くてもアプリは起動し、メトリクスとログは確かめられます。そのときは送信時の接続エラーがログに出ることがありますが、動作確認だけなら無視して構いません。

トレースを画面で見る場合は、Docker Desktop を起動してから Jaeger（OTLP 受信を内蔵）を立てます。

```bash
docker run --rm -d --name jaeger -p 4318:4318 -p 16686:16686 jaegertracing/all-in-one:latest
```

リクエストを何回か送り、`http://localhost:16686` でサービス名 `spring-observability-employee` を選ぶと、HTTP 受信 → `employee.get` → SQL 発行がスパンの階層として表示されます。送信はバッファリングされるため、すぐに表示されるとは限りません。使い終わったら `docker stop jaeger` で止めます。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/rest/service/EmployeeService.java` … Observation API と `@Observed` による計装
- `src/main/java/pro/kensait/spring/employee/rest/observation/ObservationConfig.java` … `ObservedAspect` の登録
- `src/main/resources/application.yml` … サンプリング率、OTLP の送信先、traceId／spanId を含むログ形式
