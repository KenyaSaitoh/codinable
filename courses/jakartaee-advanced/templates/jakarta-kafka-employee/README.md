# jakarta-kafka-employee — Kafka のクライアントライブラリによる送受信

チャプター 8.1「Jakarta Messagingによる非同期メッセージング」のレッスン 8.1.5 の演習です。
Kafka の公式のクライアントライブラリ（`kafka-clients`）を WAR に含め、Jakarta EE のアプリケーションから直接使います。
社員イベントを送信する `producer`（このフォルダ直下）と、受信する `consumer`（`consumer/`）が、トピック `employee-events` を介して連携します。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 送信側（`http://localhost:8080/jakarta-kafka-employee/api/events`） | ▶ アプリ実行 |
| `consumer/` | 受信側（`http://localhost:8080/jakarta-kafka-employee-consumer/api/events` が受信履歴） | ▶ アプリ実行で同じ Payara Server に一緒にデプロイ |
| Codinable 同梱の Kafka | ブローカー（127.0.0.1:9092） | ▶ アプリ実行の前に自動で起動（出力欄の右に「Kafka」が出る） |

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/config/KafkaSettings.java` | 送信側・受信側の設定（`acks=all`・`enable.idempotence=true`・`enable.auto.commit=false` など） |
| `src/main/java/pro/kensait/jakartaee/employee/service/EventSendService.java` | `KafkaProducer` の生成（`@PostConstruct`）・送信（キーは社員ID）・後片付け（`@PreDestroy`） |
| `consumer/src/main/java/pro/kensait/jakartaee/employee/service/EmployeeEventConsumer.java` | 起動時に `ManagedExecutorService` で受信ループを開始し、`commitSync` と `seek` でオフセットを扱う |
| `consumer/src/main/java/pro/kensait/jakartaee/employee/service/EmployeeEventHandler.java` | `eventId` による処理済みの確認（DB を先にコミットし、その後でオフセットをコミットする） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。Kafka が起動し、受信側・送信側の順にデプロイされる。プレビューに受信履歴が開く
2. `curl/01_send_event.sh` を開いて、エディタの右上の ▶ 実行を押す。同じ `eventId` のイベントを 2 回送り、受信履歴を確かめる

## 結果のどこを見るか

- 送信の応答（202）に、記録されたパーティションとオフセットが入る。同じイベントを 2 回送ると、Kafka には 2 件とも別のオフセットで残る
- 受信履歴には同じ `eventId` が 1 件だけ。実行結果には 2 件目の `処理済みのイベントです` が出る（べき等プロデューサーが防ぐのは内部の再試行による重複だけで、アプリケーションが送り直した記録は受信側で見分ける）
- 実行結果の `[ EmployeeEventConsumer#processAll ]` の `key=10001` は、記録のキー（社員ID）。同じキーの記録は同じパーティションに入る
- Kafka を出力欄の右の「Kafka」の停止で止めてから送ると、6 秒以内に送れず 503 になる
