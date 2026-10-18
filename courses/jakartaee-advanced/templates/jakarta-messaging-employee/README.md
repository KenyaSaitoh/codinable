# jakarta-messaging-employee — Jakarta Messaging とメッセージ駆動Bean

チャプター 8.1「Jakarta Messagingによる非同期メッセージング」（レッスン 8.1.1〜8.1.4）の演習です。
社員イベントを送信する `producer`（このフォルダ直下）と、受信する `consumer`（`consumer/`）の 2 つのアプリケーションが、
キュー `EmployeeEventQueue` を介して連携します。ブローカーは Payara Server に内蔵の OpenMQ で、別のメッセージング製品は使いません。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 送信側（`http://localhost:8080/jakarta-messaging-employee/api`） | ▶ アプリ実行 |
| `consumer/` | 受信側（`http://localhost:8080/jakarta-messaging-employee-consumer/api/events` が受信履歴） | ▶ アプリ実行で同じ Payara Server に一緒にデプロイ |

DB は同梱の HSQLDB（インメモリ）で、送信側は EMPLOYEE、受信側は RECEIVED_EVENT を使います。▶ アプリ実行のたびに初期状態に戻ります。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/config/MessagingConfig.java` | `@JMSDestinationDefinition` によるキューの定義（受信側にも同じものがある） |
| `src/main/java/pro/kensait/jakartaee/employee/service/EventSendService.java` | `JMSContext` による送信（永続メッセージ、プロパティ `eventType`、本文は JSON） |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeRegistrationService.java` | 社員の登録とイベントの送信を 1 つのトランザクションで確定する |
| `consumer/src/main/java/pro/kensait/jakartaee/employee/listener/EmployeeEventListener.java` | メッセージ駆動Bean（`@MessageDriven`・`onMessage`） |
| `consumer/src/main/java/pro/kensait/jakartaee/employee/service/EmployeeEventHandler.java` | `eventId` による処理済みの確認（べき等性、`MANDATORY`） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。受信側、送信側の順にデプロイされ、プレビューに受信履歴（最初は空の `[]`）が開く
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で順に流す
   - `01_send_event.sh` … イベントを送ると 202。受信側の履歴に同じ `eventId` が記録される
   - `02_register.sh` … 給与が範囲内の登録は 201 で、イベントも届く。範囲外は 400 で、登録もイベントも取り消される
   - `03_duplicate.sh` … 同じ `eventId` を 2 回送っても、受信側の記録は 1 件だけ
   - `04_poison.sh` … JSON として読めない本文は、ロールバックされて再配送され、上限を超えるとデッドレターキューへ移る
     （読めない本文は、教材用に足した `POST /api/events/raw` と `PoisonMessageSender` で送る。どちらも講座のコードには無い）

## 結果のどこを見るか

- 202 は、ブローカーがメッセージを受け付けたことを示すだけで、受信側の処理の完了ではない
- 実行結果の `[ EmployeeEventListener#onMessage ]` の `JMSXDeliveryCount` は、最初の配送で 1。`04_poison.sh` では再配送で 2 になる
  （Payara の場合、再配送の上限を超えると `marked for routing to the DMQ` が出て、メッセージはデッドレターキュー `mq.sys.dmq` へ移され、それ以上は配送されない）
- `03_duplicate.sh` の 2 回目は `JMSMessageID` が別の値になるが、`eventId` が同じなので `処理済みのイベントです` となる
