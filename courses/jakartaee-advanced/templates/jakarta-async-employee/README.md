# jakarta-async-employee — 非同期処理とサーバー送信イベント

チャプター 7.1「非同期処理とサーバー送信イベント」の演習です。社員管理システムを題材に、
Jakarta Concurrency による部署ごとの並行集計、非同期 CDI イベントによる異動の通知、
Jakarta REST の非同期応答とサーバー送信イベント（SSE）による配信を動かします。
社員は同梱の HSQLDB（インメモリ）の EMPLOYEE テーブルにあり、デプロイのたびに 16 件の初期データへ戻ります。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 社員API と確認用の画面（`http://localhost:8080/jakarta-async-employee/`） | ▶ アプリ実行 |
| `client/` | SSE を `SseEventSource` で受信する Java コンソール（`EmployeeStreamClient`） | `client/run.sh` を開いてエディタの右上の ▶ 実行 |

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/config/ConcurrencyConfig.java` | `@ContextServiceDefinition`・`@ManagedExecutorDefinition`（仮想スレッド、`maxAsync = 8`） |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeSummaryService.java` | `supplyAsync` と `allOf` による部署ごとの並行集計 |
| `src/main/java/pro/kensait/jakartaee/employee/service/DepartmentReportService.java` | `@Asynchronous`（Jakarta Concurrency）によるメソッドの非同期化 |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeService.java` | 異動の登録と、同期イベントの発火（`fire`） |
| `src/main/java/pro/kensait/jakartaee/employee/event/TransferEventRelay.java` | コミット後（`AFTER_SUCCESS`）に非同期イベントへ中継（`fireAsync`） |
| `src/main/java/pro/kensait/jakartaee/employee/event/TransferHistoryRecorder.java`・`TransferMailNotifier.java`・`TransferBroadcaster.java` | 3 つの非同期オブザーバー（履歴の記録・メール通知・SSE の一斉配信） |
| `src/main/java/pro/kensait/jakartaee/employee/api/DepartmentSummaryResource.java` | `AsyncResponse` と `CompletionStage` による非同期応答 |
| `src/main/java/pro/kensait/jakartaee/employee/api/EmployeeStreamResource.java` | SSE の有限の配信（`/employees/stream`）と一斉配信の購読（`/employees/events`） |
| `client/src/main/java/pro/kensait/jakartaee/employee/client/EmployeeStreamClient.java` | `SseEventSource` による SSE の受信 |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。プレビューに確認用の画面が開く
2. 画面で、部署ごとの集計、社員一覧の逐次配信（約 1 秒ごとに 1 件、最後に `complete`）、異動の登録を試す。
   別の Webブラウザでも同じ画面を開いておくと、異動の `transferred` イベントが両方に届く
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で流す
   - `01_summary.sh` … 3 通りの非同期応答（`AsyncResponse`・`CompletionStage`・`@Asynchronous`）
   - `02_stream.sh` … SSE の社員一覧（約 16 秒）
   - `03_transfer.sh` … 異動の登録、SSE の `transferred`、異動履歴
4. `client/run.sh` を開いて ▶ 実行すると、Java のクライアントが SSE を受信して 16 件と `complete` を確かめる

## 結果のどこを見るか

- `01_summary.sh` のあと、実行結果の `[ EmployeeSummaryService#summarize ]` の `thread=` に `VirtualThread` が出る（`virtual = true` の要求が通っている）
- 異動の応答は、メール通知や履歴の記録を待たずに返る。実行結果には `TransferEventRelay#relay` のあとに、
  `TransferMailNotifier#notify` と `TransferHistoryRecorder#record` が、発火側（要求を処理したスレッド）とは別の仮想スレッドで出る
- `src/main/resources/META-INF/microprofile-config.properties` の `transfer.mail.fail` を `true` にして ▶ アプリ実行し直すと、
  メール通知の失敗が「異動通知の一部が失敗しました」として記録され、異動履歴の記録は止まらない
- 部署なしの Victor（10014）は、4 つの部署のどの集計にも入らない
