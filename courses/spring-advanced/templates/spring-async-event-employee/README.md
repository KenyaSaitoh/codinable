# spring-async-event-employee — @Async とコミット後の非同期イベント

チャプター 7.1「リアクティブ処理と非同期イベント」の演習です。社員管理システムを題材に、
仮想スレッドのエグゼキューターによる部署ごとの並行集計、`@Async` によるメソッドの非同期化、
`ApplicationEventPublisher` と `@TransactionalEventListener(phase = AFTER_COMMIT)` による異動の通知を動かします。
社員はインメモリの HSQLDB にあり、起動のたびに 16 件の初期データへ戻ります。

## 読む順番

1. `AsyncConfig.java` … `@EnableAsync`、仮想スレッドのエグゼキューター、`void` の `@Async` の例外の受け取り
2. `EmployeeSummaryService.java` → `DepartmentReportService.java` … エグゼキューターを直接使う書き方と `@Async` の書き方
3. `EmployeeService.java` … トランザクションの中でイベントを発行する（ここでは通知しない）
4. `TransferMailNotifier.java`・`TransferHistoryRecorder.java` … コミットのあとに別のスレッドで動くリスナー
5. `TransferEventTest.java` … コミットしたときだけ通知されることの確認

## 主なファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/spring/employee/config/AsyncConfig.java` | `@EnableAsync`・`SimpleAsyncTaskExecutor`（仮想スレッド、同時に 8 タスク）・`AsyncUncaughtExceptionHandler` |
| `src/main/java/pro/kensait/spring/employee/service/EmployeeSummaryService.java` | `supplyAsync` と `allOf` による部署ごとの並行集計 |
| `src/main/java/pro/kensait/spring/employee/service/DepartmentReportService.java` | `@Async` と戻り値の `CompletableFuture`（別の Bean から呼んだときだけ非同期になる） |
| `src/main/java/pro/kensait/spring/employee/service/EmployeeService.java` | 異動の登録と `publishEvent` |
| `src/main/java/pro/kensait/spring/employee/event/TransferMailNotifier.java` | `@Async`＋`@TransactionalEventListener(phase = AFTER_COMMIT)`。メール通知の代わりにログへ出す |
| `src/main/java/pro/kensait/spring/employee/event/TransferHistoryRecorder.java` | 同じ組み合わせに `@Transactional(propagation = REQUIRES_NEW)` を足し、異動履歴を書き込む |
| `src/main/java/pro/kensait/spring/employee/web/DepartmentSummaryApi.java`・`TransferApi.java` | 戻り値の `CompletableFuture` による非同期応答と、異動の API |
| `src/test/java/pro/kensait/spring/employee/TransferEventTest.java` | コミットとロールバックでのリスナーの動き |
| `src/test/java/pro/kensait/spring/employee/TransferMailFailureTest.java` | 通知が失敗しても履歴の記録は止まらないこと |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す（ポート 8098）。プレビューに部署ごとの集計の JSON が開く
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で流す
   - `01_summary.sh` … 部署ごとの並行集計と、`@Async` のメソッドの結果
   - `02_transfer.sh` … 異動の登録 200、異動履歴、存在しない部署への異動 400（ロールバック）、存在しない社員 404
3. `TransferEventTest.java` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

- `01_summary.sh` のあと、実行結果の `[ EmployeeSummaryService#summarize ]` の `thread=` に `VirtualThread[#...,employee-1]` のように出る。部署ごとに別のスレッドで動いている
- `02_transfer.sh` の 1 つ目のあと、実行結果に `TransferMailNotifier#notify` と `TransferHistoryRecorder#record` が、要求を処理したスレッド（`nio-8098-exec-...`）とは別の仮想スレッドで出る。異動の応答はこれらを待たずに返る
- `02_transfer.sh` の 3 つ目は、イベントを発行したあとのコミットで外部キー制約に違反する。実行結果には `TransferMailNotifier#notify` が出ず、4 つ目の異動履歴も増えない
- `TransferEventTest` の各テスト
  - `commitNotifiesOnAnotherVirtualThread` … コミットすると、呼び出し元とは別の仮想スレッドで通知と履歴の記録が動く
  - `notNotifiedBeforeCommit` … イベントを発行しても、コミットするまでは通知されない
  - `constraintViolationAtCommitDoesNotNotify`・`outerRollbackDoesNotNotify` … ロールバックすると、通知も履歴の記録も起きず、部署も元のまま
- `src/main/resources/application.yml` の `transfer.mail.fail` を `true` にして ▶ アプリ実行し直すと、メール通知の失敗が「異動通知の一部が失敗しました」として記録され、異動の応答は 200 のままで、異動履歴の記録も止まらない
- 部署なしの Victor（10014）は、4 つの部署のどの集計にも入らない
