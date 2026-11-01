# database-rider-employee：データセットによる前提データと保存結果（チャプター5.1）

社員管理の REST API（画面なし）を実 HTTP で呼び出し、Database Rider（DBUnit）で
前提データと実行後の DB 状態を YAML で管理します。DB は組み込みの H2（インメモリ）です。
テーブルの構成と初期データは下の「この演習で使うテーブル」にまとめてあります。

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

| 操作 | 内容 |
|---|---|
| `EmployeeDatabaseRiderTest.java` を開いて、エディタの右上の ▶ 実行 | アプリをランダムポートで起動し、6件を実行してテスト結果を表示 |
| ツールバーの ▶ アプリ実行 | API を <http://localhost:8080> で起動する（通常起動では社員16人。テスト時は YAML の社員2人から始まる） |

元のサンプルではこのテストが `@Tag("it")` のため通常の `test` から除外され、`integrationTest` で
流していました。Codinable でテストクラスを ▶ 実行したとき（test タスク）に走るよう、除外タグを外しています（`build.gradle` のコメント参照）。

▶ アプリ実行で起動している間は、`curl/01_employees.sh`・`curl/02_masters.sh` を開いてエディタの右上の ▶ 実行で送ると、
API の応答を確かめられます（社員の一覧・1 件取得・部署と職種の一覧）。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「Database Riderで前提データと保存結果を比べる」のコードが読み書きするテーブルです。
テーブルはアプリの起動時に、インメモリの HSQLDB へ `src/main/resources/schema.sql` と `src/main/resources/data.sql` から作られます。

### DEPARTMENT

| カラム | 型 | 制約 |
|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL |
| `LOCATION` | `VARCHAR(50)` | NOT NULL |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION |
|---|---|---|
| 1 | PLANNING | TOKYO HQ |
| 2 | HR | TOKYO HQ |
| 3 | SALES | YOKOHAMA BRANCH |
| 4 | PRODUCT | YOKOHAMA BRANCH |

### JOB

| カラム | 型 | 制約 |
|---|---|---|
| `JOB_ID` | `INT` | 主キー |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL |
| `GRADE` | `INT` | NOT NULL |

初期データ（4 件）

| JOB_ID | JOB_NAME | GRADE |
|---|---|---|
| 1 | ASSOCIATE | 1 |
| 2 | CHIEF | 2 |
| 3 | LEADER | 3 |
| 4 | MANAGER | 4 |

### EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 |
| `EMPLOYEE_CODE` | `VARCHAR(8)` | NOT NULL |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` |
| `JOB_ID` | `INT` | 外部キー → `JOB.JOB_ID`、NOT NULL |
| `SALARY` | `INT` | NOT NULL |
| `ENTRANCE_DATE` | `DATE` | NOT NULL |
| `STATUS` | `VARCHAR(10)` | NOT NULL、既定値 'active' |
| `VERSION` | `INT` | NOT NULL、既定値 0 |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_CODE | EMPLOYEE_NAME | DEPARTMENT_ID | JOB_ID | SALARY | ENTRANCE_DATE | STATUS | VERSION |
|---|---|---|---|---|---|---|---|---|
| 10001 | E10001 | Alice | 3 | 4 | 500000 | 2012-04-01 | active | 0 |
| 10002 | E10002 | Bob | 1 | 4 | 450000 | 2012-04-01 | active | 0 |
| 10003 | E10003 | Carol | 2 | 2 | 350000 | 2012-04-01 | active | 0 |
| 10004 | E10004 | Dave | 3 | 3 | 400000 | 2012-04-01 | active | 0 |
| 10005 | E10005 | Ellen | 3 | 2 | 300000 | 2013-04-01 | active | 0 |
| 10006 | E10006 | Frank | 1 | 1 | 250000 | 2013-10-01 | active | 0 |
| 10007 | E10007 | Ivan | 4 | 4 | 480000 | 2014-01-01 | active | 0 |
| 10008 | E10008 | Justin | 2 | 4 | 460000 | 2014-04-01 | active | 0 |
| 10009 | E10009 | Mallory | 4 | 3 | 420000 | 2014-07-01 | active | 0 |
| 10010 | E10010 | Matilda | 3 | 1 | 280000 | 2015-08-01 | active | 0 |
| 10011 | E10011 | Oscar | 4 | 2 | 320000 | 2015-11-01 | active | 0 |
| 10012 | E10012 | Pat | 4 | 1 | 240000 | 2016-04-01 | active | 0 |
<!-- schema:end -->
