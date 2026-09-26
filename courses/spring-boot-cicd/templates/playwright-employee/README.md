# playwright-employee：Playwright による画面操作と状態の検証（チャプター6.1）

本物の Chromium を操作し、社員管理 Web アプリを画面から DB まで E2E で検証します。

## テスト

| ファイル | 内容 |
|---|---|
| `src/test/java/pro/kensait/spring/employee/e2e/EmployeePlaywrightTest.java` | 登録・編集・削除、入力エラーと入力値の保持、検索、ページ切り替えなど9ケース |
| `src/test/java/pro/kensait/spring/employee/e2e/EmployeePage.java` | Page Object。ラベル・role・名前で要素を特定する |

テストクラスで Browser を共有し、ケースごとに新しい BrowserContext と Page を作ります。
追加した社員は `@Sql(AFTER_TEST_METHOD)` で片付けます。

## Codinable での動かし方

| 実行対象 | 内容 |
|---|---|
| `test`（既定） | Chromium を用意し、アプリをランダムポートで自動起動して9ケースを実行する |
| `bootRun` | アプリを起動する。プレビューで <http://localhost:8080/employees> を開き、手で操作できる |

- **テストのために bootRun しておく必要はありません**。テストが自分でアプリを起動します
- **初回の test は Chromium のダウンロード（百数十 MB）が走るため時間がかかります**。
  インターネット接続が必要です。2回目以降はすぐ始まります
- 元のサンプルはこのテスト（`@Tag("e2e")`）を専用タスク `e2eTest` で流していました。
  Codinable の「test」で走るよう、その設定を `test` に移しています（`build.gradle` のコメント参照）

## 失敗の調査

`build/reports/playwright/` に、ケースごとの画面（`<テストメソッド名>.png`）と
Trace（`<テストメソッド名>.zip`）を保存します。成功時も保存されます。
Trace は、一度「実行」を押して `gradlew.bat` ができた後、ターミナルから開けます。

```bash
./gradlew.bat showPlaywrightTrace
./gradlew.bat showPlaywrightTrace -Ptrace=build/reports/playwright/failedEditKeepsStoredValuesAndCanBeCorrected.zip
```

ブラウザを表示して動きを見たい場合:

```bash
./gradlew.bat test -Dplaywright.headless=false --rerun-tasks
```
