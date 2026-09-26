# database-rider-employee：データセットによる前提データと保存結果（チャプター5.1）

社員管理の REST API（画面なし）を実 HTTP で呼び出し、Database Rider（DBUnit）で
前提データと実行後の DB 状態を YAML で管理します。DB は組み込みの H2（インメモリ）です。

## テスト

`src/test/java/pro/kensait/spring/employee/database/EmployeeDatabaseRiderTest.java` の6件です。

| ケース | 確かめること |
|---|---|
| 検索 | `@DataSet` の YAML から読み込んだ社員を条件付きで取得できる |
| 登録 | HTTP 201 と Location、採番・社員コード・初期 version を含む DB 状態（`@ExpectedDataSet`） |
| 更新 | 更新項目と version の加算 |
| 論理削除 | HTTP 204、削除後の取得404、行を残して status と version を変更 |
| 古い version | HTTP 409、DB が変更されない |
| 存在しない部署 | HTTP 400、社員を追加しない |

前提データは `src/test/resources/datasets/employees.yml`、期待データは
`employees-created.yml` / `employees-updated.yml` / `employees-deleted.yml` です。
テストに `@Transactional` は付けず、API 内のトランザクションがコミットした後の DB を比較します。

## Codinable での動かし方

| 実行対象 | 内容 |
|---|---|
| `test`（既定） | アプリをランダムポートで起動し、6件を実行してテスト結果を表示 |
| `bootRun` | API を <http://localhost:8080> で起動する（通常起動では社員10人。テスト時は YAML の社員2人から始まる） |

元のサンプルではこのテストが `@Tag("it")` のため通常の `test` から除外され、`integrationTest` で
流していました。Codinable の「test」で走るよう、除外タグを外しています（`build.gradle` のコメント参照）。

bootRun 中は、ターミナルから API を確かめられます。

```bash
curl http://localhost:8080/employees
curl http://localhost:8080/employees/1
curl http://localhost:8080/departments
curl http://localhost:8080/jobs
```
