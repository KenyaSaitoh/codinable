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

| 操作 | 内容 |
|---|---|
| `EmployeePlaywrightTest.java` を開いて、エディタの右上の ▶ 実行 | Chromium を用意し、アプリをランダムポートで自動起動して9ケースを実行する |
| ツールバーの ▶ アプリ実行 | アプリを起動する。プレビューで <http://localhost:8080/employees> を開き、手で操作できる |

- **テストのために ▶ アプリ実行で起動しておく必要はありません**。テストが自分でアプリを起動します
- **初回のテストは Chromium のダウンロード（百数十 MB）が走るため時間がかかります**。
  インターネット接続が必要です。2回目以降はすぐ始まります
- 元のサンプルはこのテスト（`@Tag("e2e")`）を専用タスク `e2eTest` で流していました。
  テストクラスの ▶ 実行（test タスク）で走るよう、その設定を `test` に移しています（`build.gradle` のコメント参照）

## 失敗の調査

`build/reports/playwright/` に、ケースごとの画面（`<テストメソッド名>.png`）と
Trace（`<テストメソッド名>.zip`）を保存します。成功時も保存されます。
Trace は、テストを流した後に `scripts/01_show_trace.sh` を開いて、エディタの右上の ▶ 実行で開けます
（Playwright の Trace Viewer が開きます。既定は `registersEditsAndDeletesEmployee.zip`。
別の Trace はスクリプトの `-Ptrace=` の例を参考に書き換えます）。

ブラウザを表示して動きを見たい場合は、`scripts/02_test_headed.sh` を ▶ 実行します
（`gradlew.bat test -Dplaywright.headless=false --rerun-tasks`）。
