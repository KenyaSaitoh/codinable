# spring-kafka-employee — Kafka による社員イベントの送受信

社員イベントを REST で受け付け（プロデューサー）、Kafka を経由して別の Spring アプリ（コンシューマー）へ配送します。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | プロデューサー（`spring-kafka-producer`、8091） | 実行対象 `gradle:bootRun` で「実行」 |
| `consumer/` | コンシューマー（`spring-kafka-consumer`、8092） | 「実行」で一緒に起動 |

## Kafka について（Codinable での変更点）

講座では docker compose の Kafka（`localhost:9094`）を使います。Codinable では**同梱の Kafka（`127.0.0.1:9092`）**を使うため、Docker は不要です。

- `codinable.services.json` に `kafka` を書いてあるので、「実行」を押すと Kafka が自動で起動します（初回は少し時間がかかります）。起動・停止・ログは「メッセージング」タブで確認できます。
- 接続先は `application.yml` の `spring.kafka.bootstrap-servers`（既定 `127.0.0.1:9092`）。実行時とターミナルには Codinable が環境変数 `KAFKA_BOOTSTRAP_SERVERS`・`SPRING_KAFKA_BOOTSTRAP_SERVERS` を渡します。
- トピック `employee-events`、キーは社員 ID、コンシューマーグループ `employee-group`。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押します。Kafka・プロデューサー・コンシューマー（`consumer/`）が一緒に起動します。
   - コンシューマーの出力は実行結果タブに `[consumer] ` を付けて出ます。「実行」のたびに起動し直すので、受信履歴は空から始まります。
   - 「停止」で止まるのはプロデューサーだけです。コンシューマーは、出力タブの行の右端にある「consumer停止」「consumer起動」で個別に止めたり起動したりできます。
2. コンシューマーの用意ができると、プレビューに受信履歴 `http://localhost:8092/events` が開きます。
3. ターミナルでイベントを送り、受信履歴を確かめます。

   ```bash
   curl -i -X POST http://localhost:8091/events -H 'Content-Type: application/json' \
     -d '{"eventType":"EMPLOYEE_CREATED","employeeId":1,"employeeName":"Alice","occurredAt":"2026-04-01T09:00:00"}'
   curl -i http://localhost:8092/events
   ```

   - 送信側は Kafka の確認応答を待ってから **202** を返します。202 はブローカーが受理したことを示し、コンシューマーの処理完了を保証しません。確認応答が得られないときは 503。
   - `eventType` は `EMPLOYEE_CREATED`／`EMPLOYEE_UPDATED`／`EMPLOYEE_DELETED`。入力不正は 400。
   - コンシューマーの `GET /events` は、実際に受信した直近 100 件です（再起動で消えます）。

`curlメモ.txt` は Windows のコマンドプロンプト向けの書き方です。Codinable のターミナル（bash）では上の例のように JSON を単一引用符で囲みます。

## テスト

実行対象 `gradle:test` でコンパイルとテストタスクを確かめられます（このサンプルには単体テストはありません）。

## 主なファイル

- `src/main/java/pro/kensait/spring/kafka/service/EventSendService.java` … 送信と確認応答の待機
- `consumer/src/main/java/pro/kensait/spring/kafka/service/EventReceiveService.java` … `@KafkaListener` による受信
- `src/main/resources/application.yml`・`consumer/src/main/resources/application.yml` … シリアライザー・グループ・オフセットの設定
