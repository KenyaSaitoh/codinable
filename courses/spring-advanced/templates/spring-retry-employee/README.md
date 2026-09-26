# spring-retry-employee — Spring Retry によるリトライ

Spring Retry で、呼び出し先 API への通信失敗をリトライし、指数バックオフと代替応答を確かめます。外部 DB は不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Retry のアプリ（8081） | 実行対象 `gradle:bootRun` で「実行」 |
| `api/` | 呼び出し先の確認用 API（基礎編の `rest-employee-restclient-api`、127.0.0.1:8095） | ターミナルで `../gradlew.bat bootRun` |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |

講座では基礎編リポジトリ（`learn_spring_aidd_basic`）で API を起動しますが、ここでは同じ API を `api/` に同梱しています。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押し、このアプリを起動します。
2. ターミナルで呼び出し先 API を起動します。

   ```bash
   cd api
   ../gradlew.bat bootRun
   ```

   `../gradlew.bat` は、ルートで一度「実行」を押すと Codinable が作ります。

3. 別のターミナルで呼び出します（React 画面を使う場合は `cd frontend` → `npm install` → `npm run dev` で起動し、プレビューの URL 欄に `http://localhost:5173/` を入れます）。

   ```bash
   curl -i http://localhost:8081/retry-demo
   ```

## 障害と復旧の確かめ方

1. API 稼働中に `/retry-demo` を呼び、社員 6 件と `fallback: false` を確かめます。出力タブのログには「試行 1 回目」が 1 行だけ出ます。
2. `api/` を起動したターミナルで Ctrl+C を押し、API を止めます。
3. もう一度 `/retry-demo` を呼ぶと、初回を含めて最大 3 回試行し（1 秒 → 2 秒の待機）、その後 `fallback: true` の代替応答が返ります。出力タブで「試行 1〜3 回目」と回復処理のログを確かめます。
4. API をもう一度起動すると、次の呼び出しは通常の応答に戻ります。

再試行の対象は通信例外（`ResourceAccessException`）で、すべての HTTP エラーを再試行するわけではありません。接続タイムアウトは 2 秒、読み取りタイムアウトは 3 秒なので、待機と通信時間が加わり「3 回試すから必ず 3 秒で終わる」とは限りません。curl の手順は `curlメモ.txt` にもあります。

呼び出し先は `application.yml` の `employee.api.url`（既定 `http://localhost:8095`、環境変数 `EMPLOYEE_API_URL` で変更可）。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/retry/service/EmployeeClientService.java` … `@Retryable`・`@Recover` とバックオフ
- `src/main/java/pro/kensait/spring/employee/retry/Application.java` … `@EnableRetry`
- `src/main/resources/application.yml` … 呼び出し先とログレベル
