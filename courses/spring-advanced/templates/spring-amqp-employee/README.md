# spring-amqp-employee — RabbitMQ による社員イベントの送受信

社員イベントを REST で受け付け（プロデューサー）、RabbitMQ（AMQP 0-9-1）を経由して別の Spring アプリ（コンシューマー）へ配送します。DirectExchange `employee-exchange`、routing key `employee.event`、queue `employee-queue`。publisher confirm と return でブローカーの受理と配送先の有無を確かめます。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | プロデューサー（`spring-amqp-producer`、8093） | 実行対象 `gradle:bootRun` で「実行」 |
| `consumer/` | コンシューマー（`spring-amqp-consumer`、8094） | 「実行」で一緒に起動 |
| `frontend/` | 専用の React 画面（Vite、5173） | 「実行」で一緒に起動 |

## RabbitMQ について（Codinable での変更点）

講座では docker compose の RabbitMQ を使います。Codinable では**同梱の RabbitMQ（`127.0.0.1:5672`、ローカル教材用 guest/guest）**を使うため、Docker は不要です。

- `codinable.services.json` に `rabbitmq` を書いてあるので、「実行」を押すと RabbitMQ が自動で起動します（初回は少し時間がかかります）。起動・停止・ログ・管理画面（`http://127.0.0.1:15672`、guest/guest）は「メッセージング」タブから開けます。
- 接続先は `application.yml` の `spring.rabbitmq.*`（既定 `127.0.0.1:5672`）。実行時とターミナルには Codinable が環境変数 `SPRING_RABBITMQ_HOST`・`SPRING_RABBITMQ_PORT` などを渡します。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押します。RabbitMQ・プロデューサー・コンシューマー（`consumer/`）・React 画面（`frontend/`）が一緒に起動します。`npm install` も必要なときに自動で走ります。
   - コンシューマーと React 画面の出力は、実行結果タブに `[consumer] `・`[frontend] ` を付けて出ます。「実行」のたびに起動し直すので、受信履歴は空から始まります。
   - 「停止」で止まるのはプロデューサーだけです。コンシューマーと React 画面は、出力タブの行の右端にある「consumer停止」「frontend停止」などで個別に止めたり起動したりできます。
2. React 画面の用意ができると、プレビューに `http://localhost:5173/` が開きます。イベントを送信して 202 を確かめ、「受信履歴を取得」を押します。

   画面の `/api/amqp/...` は 8093（プロデューサー）、`/api/amqp-consumer/...` は 8094（コンシューマー）へ Vite が転送します（`frontend/vite.config.ts`）。

curl で確かめる場合（bash では JSON を単一引用符で囲みます。`curlメモ.txt` は Windows のコマンドプロンプト向けの書き方です）:

```bash
curl -i -X POST http://localhost:8093/events -H 'Content-Type: application/json' \
  -d '{"eventType":"EMPLOYEE_CREATED","employeeId":1,"employeeName":"Alice","occurredAt":"2026-04-01T09:00:00"}'
curl -i http://localhost:8094/events
```

- 202 はブローカーが受理したことを示し、コンシューマーの処理完了は保証しません。確認が失敗すると 503。
- コンシューマーの `GET /events` は実際に受信した直近 100 件です（再起動で消えます）。

## 主なファイル

- `src/main/java/pro/kensait/spring/amqp/config/AmqpConfig.java` … エクスチェンジ・キュー・バインディングの定義
- `src/main/java/pro/kensait/spring/amqp/service/EventSendService.java` … 送信と publisher confirm
- `consumer/src/main/java/pro/kensait/spring/amqp/service/EventReceiveService.java` … 受信と ack
- `src/main/resources/application.yml` … publisher confirm／return の設定
