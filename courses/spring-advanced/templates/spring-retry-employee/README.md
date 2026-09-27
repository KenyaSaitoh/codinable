# spring-retry-employee — Spring Retry によるリトライ

Spring Retry で、呼び出し先 API への通信失敗をリトライし、指数バックオフと代替応答を確かめます。外部 DB は不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Retry のアプリ（8081） | 実行対象 `gradle:bootRun` で「実行」 |
| `api/` | 呼び出し先の確認用 API（基礎編の `rest-employee-restclient-api`、127.0.0.1:8095） | 「実行」で一緒に起動 |
| `frontend/` | 専用の React 画面（Vite、5173） | 「実行」で一緒に起動 |

講座では基礎編リポジトリ（`learn_spring_aidd_basic`）で API を起動しますが、ここでは同じ API を `api/` に同梱しています。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押します。このアプリ・呼び出し先 API（`api/`）・React 画面（`frontend/`）が一緒に起動します。`npm install` も必要なときに自動で走ります。
   - API と React 画面の出力は、実行結果タブに `[api] `・`[frontend] ` を付けて出ます。「実行」のたびに起動し直します。
   - 「停止」で止まるのはこのアプリだけです。API と React 画面は、出力タブの行の右端にある「api停止」「api起動」「frontend停止」などで個別に止めたり起動したりできます（障害と復旧はこのボタンで作ります）。
2. React 画面の用意ができると、プレビューに `http://localhost:5173/` が開きます。ターミナルから呼び出す場合は次のようにします。

   ```bash
   curl -i http://localhost:8081/retry-demo
   ```

## 障害と復旧の確かめ方

1. API 稼働中に `/retry-demo` を呼び、社員 16 件と `fallback: false` を確かめます。出力タブのログには「試行 1 回目」が 1 行だけ出ます。
2. 出力タブの行の右端にある「api停止」を押し、API を止めます。
3. もう一度 `/retry-demo` を呼ぶと、初回を含めて最大 3 回試行し（1 秒 → 2 秒の待機）、その後 `fallback: true` の代替応答が返ります。出力タブで「試行 1〜3 回目」と回復処理のログを確かめます。
4. 「api起動」を押して API をもう一度起動すると、次の呼び出しは通常の応答に戻ります。

再試行の対象は通信例外（`ResourceAccessException`）で、すべての HTTP エラーを再試行するわけではありません。接続タイムアウトは 2 秒、読み取りタイムアウトは 3 秒なので、待機と通信時間が加わり「3 回試すから必ず 3 秒で終わる」とは限りません。curl の手順は `curlメモ.txt` にもあります。

呼び出し先は `application.yml` の `employee.api.url`（既定 `http://localhost:8095`、環境変数 `EMPLOYEE_API_URL` で変更可）。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/retry/service/EmployeeClientService.java` … `@Retryable`・`@Recover` とバックオフ
- `src/main/java/pro/kensait/spring/employee/retry/Application.java` … `@EnableRetry`
- `src/main/resources/application.yml` … 呼び出し先とログレベル
