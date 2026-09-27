# spring-resilience-employee — サーキットブレーカーとレート制限

Resilience4j で、呼び出し先 API の障害に対するサーキットブレーカー・代替応答・レート制限を確かめます。外部 DB は不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Resilience4j のアプリ（8082） | 実行対象 `gradle:bootRun` で「実行」 |
| `api/` | 呼び出し先の確認用 API（基礎編の `rest-employee-restclient-api`、127.0.0.1:8095） | 「実行」で一緒に起動 |
| `frontend/` | 専用の React 画面（Vite、5173） | 「実行」で一緒に起動 |

講座では基礎編リポジトリ（`learn_spring_aidd_basic`）で API を起動しますが、ここでは同じ API を `api/` に同梱しています。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押します。このアプリ・呼び出し先 API（`api/`）・React 画面（`frontend/`）が一緒に起動します。`npm install` も必要なときに自動で走ります。
   - API と React 画面の出力は、実行結果タブに `[api] `・`[frontend] ` を付けて出ます。「実行」のたびに起動し直します。
   - 「停止」で止まるのはこのアプリだけです。API と React 画面は、出力タブの行の右端にある「api停止」「api起動」「frontend停止」などで個別に止めたり起動したりできます（障害と復旧はこのボタンで作ります）。
2. React 画面の用意ができると、プレビューに `http://localhost:5173/` が開きます。ターミナルから呼び出す場合は次のようにします。

   ```bash
   curl -i http://localhost:8082/resilience-demo      # 社員 16 件と "fallback":false
   curl -i http://localhost:8082/resilience-status    # 回路の状態（最初は CLOSED）
   ```

## 障害と復旧の確かめ方

1. API 稼働中に `/resilience-demo` を呼び、社員 16 件と `fallback: false` を確かめます。
2. 出力タブの行の右端にある「api停止」を押し、API を止めます。
3. `/resilience-demo` を 5 回以上呼ぶと代替応答（`fallback: true`）が返り、`/resilience-status` が `OPEN` になります。OPEN の間は API を呼ばずにすぐ代替応答を返します。
4. 「api起動」を押し、API をもう一度起動します。OPEN から 10 秒以上たってから 3 回成功すると `CLOSED` に戻ります。
5. `/ratelimit-demo` を 10 秒以内に 4 回以上呼ぶと、上限超過分が **429** になります（画面の 8 回連続実行でも確かめられます）。

設定は直近 10 回・最低 5 回・失敗率 50%、OPEN 維持 10 秒、HALF_OPEN で 3 回確認です（`src/main/resources/application.yml`）。`/actuator/circuitbreakers` と `/actuator/health` でも状態を見られます。curl の手順は `curlメモ.txt` にもあります。

呼び出し先は `application.yml` の `employee.api.url`（既定 `http://localhost:8095`、環境変数 `EMPLOYEE_API_URL` で変更可）。API のデータはメモリ内にあり、再起動で初期状態に戻ります。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/resilience/service/EmployeeClientService.java` … `@CircuitBreaker`・`@RateLimiter` と代替応答
- `src/main/resources/application.yml` … サーキットブレーカーとレート制限の設定
- `src/main/java/pro/kensait/spring/employee/resilience/web/CircuitStatusApi.java` … 回路状態の取得
