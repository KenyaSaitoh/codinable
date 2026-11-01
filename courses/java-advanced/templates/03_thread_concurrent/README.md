# 03_thread_concurrent — スレッドと並行処理

## 何を学ぶ演習か

スレッドの起動と待ち合わせ、競合状態と同期化、デッドロック、Executor フレームワーク、同期化支援を、動かして確かめます（チャプター3.1〜3.3）。

## 見るファイル（`src/main/java/pro/kensait/java/advanced/`）

- `lsn_3_1_1` … スレッドの作成（`thread`・`runnable`）、`join`、`sleep`（割り込みは `Main_Interrupt`）
- `lsn_3_1_2` … `unsafe`・`safe`（競合状態と同期化）、`deadlock`、`readwritelock`、`immutable`
- `lsn_3_1_3` … `wait()` と `notify()`、`lsn_3_1_4` … スレッドローカル
- `lsn_3_2_2`〜`lsn_3_2_5` … スレッドプール、シャットダウン、定期実行、`Future` と `Callable`、`invokeAll`
- `lsn_3_3_1` … `CountDownLatch` と `CyclicBarrier`

## ボタンでの動かし方

- 動かしたいクラスをエディタで開き、右上の **ファイル実行** を押します。結果は「実行結果」に出ます。
- ツールバーの **アプリ実行** は、選んでいるレッスンの最初の main を動かします。
- 書き換えて動かなくなったら **↺ 初期化** で配布時の状態に戻せます。

## 止める必要があるプログラム

次の main は終わりません。出力が止まった、または確かめ終わったら、出力欄の **停止** で止めます。

- `lsn_3_1_2.deadlock.Main` … 出力が止まればデッドロックです
- `lsn_3_2_2.Main_Single`・`Main_Fixed` … `shutdown()` を呼ばないので、タスクの後もプログラムが残ります
- `lsn_3_2_3.Main_FixedRate`・`Main_FixedDelay` … 定期実行を繰り返します

`lsn_3_2_2.Main_Cached` もタスクの後に約 60 秒残ってから終わります。`thread`・`runnable` は 1000 億回のループがあり、終わるまで時間がかかります。

## 結果のどこを見るか

- マルチスレッドの出力は、実行のたびに順序が変わることがあります。何度か動かして見比べます。
- `callable.Main_Timeout`（`TimeoutException`）と `lsn_3_2_4.exception.Main`（`ExecutionException`）は例外で終わります。わざと例外を見せる教材です。
- `unsafe.Main` の最終的な値は毎回変わり、`safe.Main` は常に 30 です。
- `lsn_3_1_3.Main` は 5 秒後に `result => 12345` を受け取ります。`collection.Main` の合計値は 5000050000 です。
