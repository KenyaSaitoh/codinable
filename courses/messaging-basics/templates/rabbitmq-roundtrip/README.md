# RabbitMQ: publish and consume / 送信と受信

「実行」を押すと、RabbitMQ の起動 → キュー作成 → 送信 → 受信 → ACK の順に動きます。
初回は Gradle と Java ライブラリの取得にネット接続が必要です。

Run to start RabbitMQ, declare a queue, publish a message, consume it, and acknowledge it.
The first run downloads Gradle and Java dependencies.

- 接続先 / Broker: `amqp://guest:guest@127.0.0.1:5672/` (`RABBITMQ_URL`)
- 管理画面 / Management UI: `http://127.0.0.1:15672`
- ユーザー名・パスワード / Username and password: `guest` / `guest`
- キュー / Queue: `codinable-example`

「メッセージング」タブの「管理画面」からキューの状態を確認できます。
ACK 後のメッセージはキューから削除されます。
停止後の「データを初期化」は、他の演習も含む RabbitMQ の全データを削除します。

Open the Management UI from the Messaging tab to inspect the queue. Acknowledged
messages are removed. Reset data deletes all local RabbitMQ data, including other exercises.
