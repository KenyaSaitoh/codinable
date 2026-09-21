# Kafka: send and receive / 送信と受信

「実行」を押すと、Kafka の起動 → トピック作成 → 送信 → 受信の順に動きます。
初回は Gradle と Java ライブラリの取得にネット接続が必要です。

Run the example to start Kafka, create a topic, produce a record, and consume it.
The first run downloads Gradle and Java dependencies.

- 接続先 / Broker: `127.0.0.1:9092` (`KAFKA_BOOTSTRAP_SERVERS`)
- トピック / Topic: `codinable-example`
- メッセージは停止後も保存されます。再実行すると別の consumer group で読み直します。
- Records survive a restart. Each run uses a new consumer group to replay the topic.

「メッセージング」タブでログを確認し、Kafka を停止できます。
停止後の「データを初期化」は、他の演習も含む Kafka の全データを削除します。

Use the Messaging tab to inspect logs and stop Kafka. Reset data deletes all local
Kafka topics, records, and consumer offsets, including those from other exercises.
