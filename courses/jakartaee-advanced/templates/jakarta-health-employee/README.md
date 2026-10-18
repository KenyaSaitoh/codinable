# jakarta-health-employee — MicroProfile Health とメトリクス

チャプター 10.1「観測・監視機能」のレッスン 10.1.1〜10.1.2 の演習です。
社員管理API に、MicroProfile Health の 3 種類のチェック（`@Liveness`・`@Readiness`・`@Startup`）と、
MicroProfile Telemetry の `Meter` によるカウンター `employee.get.count` を加えています。
社員は同梱の HSQLDB（インメモリ）にあり、デプロイのたびに 16 件の初期データへ戻ります。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/health/DatabaseHealthCheck.java` | `@Readiness`。社員テーブルを実際に問い合わせ、件数を `data` に付ける |
| `src/main/java/pro/kensait/jakartaee/employee/health/MemoryHealthCheck.java` | `@Liveness`。ヒープの使用率が 90% に達していないか |
| `src/main/java/pro/kensait/jakartaee/employee/health/StartupHealthCheck.java` | `@Startup`。アプリケーションのスコープの準備が整ったか |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeService.java` | `Meter` によるカウンター（`@PostConstruct` で一度だけ作る） |
| `src/main/resources/META-INF/microprofile-config.properties` | Telemetry の有効化と、メトリクスの書き出し先 |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。プレビューに `http://localhost:8080/health` が開く
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で流す
   - `01_health.sh` … `/health`・`/health/live`・`/health/ready`・`/health/started`
   - `02_employee_metrics.sh` … 社員の取得を 3 回（うち 1 回は 404）呼び、カウンターを増やす

## 結果のどこを見るか

- Payara の場合、ヘルスのエンドポイントはアプリケーションのコンテキストルート（`/jakarta-health-employee`）ではなく、サーバーのルートの `/health` にある
- 全体の `status` は、すべてのチェックの論理積。1 つでも DOWN なら全体も DOWN で、HTTP ステータスは 503 になる
- メトリクスは、講座本編では OTLP で外部の受信先へ送るが、Codinable では受信先を用意しないので、
  実行結果（Payara Server のログ）へ 60 秒ごとに書き出している（`otel.metrics.exporter=console`）。
  `02_employee_metrics.sh` のあと、`employee.get.count` の値が 3 になっていることを確かめる（存在しない社員の要求も数える）
