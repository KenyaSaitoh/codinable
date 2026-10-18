# jakarta-fault-tolerance-employee — MicroProfile Fault Tolerance による障害への対処

チャプター 9.1「MicroProfile Fault Toleranceによる障害への対処」の演習です。
Payara Server 上のアプリケーションが、MicroProfile Rest Client で別プロセスの社員API（8095）を呼び、
`@CircuitBreaker`・`@Bulkhead`・`@Timeout`・`@Retry`・`@Fallback` で遮断・同時実行数・時間・再試行・代替応答を制御します。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 呼び出し側（`http://localhost:8080/jakarta-fault-tolerance-employee/api/...`） | ▶ アプリ実行 |
| `api/` | 確認用の社員API（`http://localhost:8095/employees`。Payara Server とは別の Java のプロセス） | ▶ アプリ実行で一緒に起動。出力欄の右の「api」で停止・起動 |

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/client/EmployeeApiClient.java` | 社員API の Rest Client（`configKey = "employee-api"`） |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeClientService.java` | Fault Tolerance のアノテーションを付けた呼び出し |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeFallbackHandler.java` | `FallbackHandler` による代替処理（失敗したメソッドと例外を記録） |
| `src/main/java/pro/kensait/jakartaee/employee/web/BulkheadExceptionMapper.java` | 同時実行数の上限による拒否を 503 と `Retry-After` にする |
| `src/main/resources/META-INF/microprofile-config.properties` | 接続先とタイムアウト、アノテーションの属性の上書きの例（最初はコメントアウト） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。呼び出し側が Payara Server にデプロイされ、社員API（`api/`）も起動する
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で流す
   - `01_resilience_demo.sh` … 社員APIの稼働中は 16 件と `"fallback":false`
   - `02_circuit_breaker.sh` … 「api」を停止してから流すと、代替応答が返り、5 回目からは社員API を呼ばずに遮断される
   - `03_bulkhead.sh` … 社員APIの応答を 1.5 秒遅らせて 5 件ずつ同時に送る。同期版は 2 件だけ 200、残りは 503。非同期版は 5 件とも 200
   - `04_retry.sh` … 「api」を停止してから流すと、初回と再試行 2 回の計 3 回呼んでから代替応答になる
3. 「api」を起動し直し、10 秒以上たってから `01_resilience_demo.sh` を 2 回流すと、回路が CLOSED に戻って社員を取得できる

## 結果のどこを見るか

- 代替応答は HTTP 200 でも `"fallback":true`。空のリストだけを見て「社員が 0 人」と判断しない
- 遮断（OPEN）の間は、実行結果に `[ EmployeeClientService#getEmployees ]` が出ずに `fallbackEmployees` だけが出る（社員API へ通信していない）
- `04_retry.sh` の `[ EmployeeClientService#getEmployeesWithRetry ]` は約 1 秒（800〜1200 ミリ秒）間隔で 3 回出て、最後に `EmployeeFallbackHandler#handle` に失敗の例外名が出る
- 回路の状態はアプリケーションのメモリにあるので、▶ アプリ実行し直すと CLOSED から始まる
- Payara の場合（7.2026.9 で確認）、`@CircuitBreaker` の `delayUnit = ChronoUnit.SECONDS` が効かず `delay = 10` が 10 ミリ秒になるため、
  `microprofile-config.properties` で同じ 10 秒をミリ秒（`delay=10000`・`delayUnit=MILLIS`）として与え直している
- 回路の状態の変化は、`http://localhost:8080/metrics` の `ft_circuitbreaker_calls_total`（`circuitBreakerOpen` が遮断された回数）や
  `ft_circuitbreaker_opened_total` でも確かめられる
