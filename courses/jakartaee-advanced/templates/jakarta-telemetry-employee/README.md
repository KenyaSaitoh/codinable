# jakarta-telemetry-employee — MicroProfile Telemetry によるトレース

チャプター 10.1「観測・監視機能」のレッスン 10.1.3〜10.1.5 の演習です。
社員管理API の社員取得を、自動計装（HTTP サーバーのスパン）、`@WithSpan`、`Tracer` で計装し、
ログにトレースIDとスパンIDを出力します。社員は同梱の HSQLDB（インメモリ）にあり、デプロイのたびに 16 件の初期データへ戻ります。

講座本編ではスパンを OTLP で Jaeger へ送りますが、Codinable では外部の受信先を用意しないので、
スパンを実行結果（Payara Server のログ）へ書き出す設定にしています（`otel.traces.exporter=console`）。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/resources/META-INF/microprofile-config.properties` | Telemetry の有効化（`otel.sdk.disabled=false`）とスパンの書き出し先 |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeService.java` | `@WithSpan("employee.get")`・`@SpanAttribute`、`Tracer` による `employee.get-all`、`logWithTrace` |
| `src/main/java/pro/kensait/jakartaee/employee/web/EmployeeResource.java` | 社員API（自動計装される Jakarta REST のリソース） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。プレビューに `http://localhost:8080/jakarta-telemetry-employee/api/employees/10001` の JSON が開く
2. `curl/01_trace.sh` を開いて、エディタの右上の ▶ 実行を押す。`traceparent` ヘッダーを付けた社員取得、全件取得、存在しない社員の取得を送る

## 結果のどこを見るか

実行結果（スパンはまとめて書き出されるので、数秒遅れることがある）で、次を確かめます。

- `[ EmployeeService#getEmployee ] traceId=... spanId=...` のログと、`employee.get` のスパンのトレースIDが同じ
- `employee.get` のスパンと `GET /jakarta-telemetry-employee/api/employees/{employeeId}` のスパン（Payara の場合、名前にコンテキストルートが入る）は同じトレースIDを持ち、`employee.get` が子になる
- `traceparent` ヘッダーで送ったトレースID `4bf92f3577b34da6a3ce929d0e0e4736` が、そのまま引き継がれている
- `employee.get` のスパンには `employee.id=10001` と `employee.type` の属性、`employee.get-all` には `employee.count=16` が付く
