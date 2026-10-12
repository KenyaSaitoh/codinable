# api-test-employee：RestClient による HTTP 契約の検証（チャプター5.1）

社員管理の REST API を `@SpringBootTest(webEnvironment = RANDOM_PORT)` の実サーバーとして起動し、
RestClient でステータス・JSON・保存後の DB 状態を検証します。

## テスト

`src/test/java/pro/kensait/spring/employee/rest/api/EmployeeApiTest.java` です。
登録結果を使った再取得、サーバー管理項目の保護、エラーレスポンス、論理削除後の読み取り経路、
逐次更新と同時更新（古い version の拒否）を確かめます。
各テストの前に `@Sql` で `reset-employees.sql` と初期データを流し、前提をそろえます。

## Codinable での動かし方

| 操作 | 内容 |
|---|---|
| `EmployeeApiTest.java` を開いて、エディタの右上の ▶ 実行 | アプリと HSQLDB（インメモリ）をテストが自動で起動し、テスト結果を表示 |
| ツールバーの ▶ アプリ実行 | API を起動する。プレビューで <http://localhost:8080/> を開くと、API を呼ぶ簡単な画面（`static/index.html`）が出る |

元のサンプルは HSQLDB サーバー（`localhost:9001`）に接続する設定でした。Codinable では
HSQLDB サーバーを使わず、▶ アプリ実行（bootRun）でもインメモリ（`jdbc:hsqldb:mem:testdb`）で起動し、
`sql/hsqldb/2_EMPLOYEE_DDL.sql` と `3_EMPLOYEE_DML.sql` を流します（`application.yml` のコメント参照）。

▶ アプリ実行で起動している間は、`curl/01_employees.sh`・`curl/02_masters.sh` を開いてエディタの右上の ▶ 実行で送ると、
API の応答を確かめられます（社員の一覧・1 件取得・部署の一覧）。

API の仕様は `swagger.yaml` にあります。
