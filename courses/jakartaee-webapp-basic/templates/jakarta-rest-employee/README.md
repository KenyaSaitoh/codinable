# jakarta-rest-employee — Jakarta REST による社員管理 API

チャプター 13.1「Jakarta RESTによるREST API」の社員管理 API です。エンティティ・Repository・Service はチャプター 10.1 の
Jakarta Persistence 版と同じで、HTTP の入出力をリソースクラス `EmployeeResource` が受け持ちます。
JSON の本文は入力専用の `EmployeeRequest` と応答用の `EmployeeResponse` で受け渡し、例外は `ExceptionMapper` で HTTP の応答に変換します。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/employee/` の下にあります。

| ファイル | 内容 |
|---|---|
| `rest/RestApplication.java` | `@ApplicationPath("/api")` |
| `rest/EmployeeResource.java` | 一覧・1 件・登録・更新・削除・月給の下限での検索。`evaluatePreconditions` による ETag の判定と、`UriInfo` による Location |
| `rest/EmployeeRequest.java`、`EmployeeResponse.java`、`EmployeeSearchParam.java` | 入力専用の DTO（Bean Validation の制約）、応答の DTO、`@BeanParam` の検索条件 |
| `rest/MasterDataResource.java` | `GET /api/departments`、`GET /api/jobs` |
| `rest/mapper/` | `ConstraintViolationException`・`InvalidRequestException`・`RangeException`・本文の JSON を読み取れない `ProcessingException` → 400、`EmployeeNotFoundException` → 404、`ConflictException` → 409 |
| `rest/ApiError.java`、`InvalidRequestException.java` | エラー本文の共通の形と、既存データと整合しない要求を表す例外 |
| `service/EmployeeService.java`、`repository/`、`entity/` | チャプター 10.1 と同じ業務処理・データアクセス・エンティティ（`@Version` で競合を検出） |
| `config/DataSourceConfig.java`、`config/DatabaseInitializer.java`、`sql/hsqldb/` | データソースの定義と、起動時にテーブルを作り直す演習用の処理 |
| `curl/01_masters.sh` 〜 `curl/09_invalid_request.sh` | API を呼び出すスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-rest-employee/api/employees` の JSON が開く
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で 1 つずつ流す
   （06〜08 はデータを変える。▶ アプリ実行でデプロイし直すと初期データに戻る）

## 結果のどこを見るか

| メソッドとパス | 成功時 | 内容 |
|---|---|---|
| GET /api/employees | 200 | 検索条件とページ（1 始まり、5 件）による一覧。`content`・`totalPages`・`totalElements` |
| GET /api/employees/{employeeId} | 200 | 社員 1 件と ETag（version の値） |
| POST /api/employees | 201 | 登録結果と Location |
| PUT /api/employees/{employeeId} | 200 | 更新結果と新しい ETag |
| DELETE /api/employees/{employeeId} | 204 | 論理削除。本文なし |
| GET /api/employees/query_by_salary?lowerSalary=500000 | 200 | 下限以上の月給の社員の配列 |
| GET /api/departments、GET /api/jobs | 200 | マスターの配列 |

- 失敗は `{"status":…,"message":…,"errors":[…]}` の形で返る。入力の誤り（400）、対象の不在（404）、更新の競合（409）を区別している
- `05_get_etag.sh`: `If-None-Match` に今の ETag を付けると 304
- `06_create_update.sh`: 古い version を送った 2 回目の更新は 409
- `07_if_match.sh`: `If-Match` に古い ETag を付けた更新は、サービスに進む前に 412

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「Jakarta REST の社員管理 API」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_EMPLOYEE_DDL.sql`、初期データ: `sql/hsqldb/3_EMPLOYEE_DML.sql`）。

### DEPARTMENT

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION |
|---|---|---|
| 1 | PLANNING | TOKYO HQ |
| 2 | HR | TOKYO HQ |
| 3 | SALES | YOKOHAMA BRANCH |
| 4 | PRODUCT | YOKOHAMA BRANCH |

### JOB

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `JOB_ID` | `INT` | 主キー | 役職ID |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL | 役職名 |
| `GRADE` | `INT` | NOT NULL | 等級 |

初期データ（4 件）

| JOB_ID | JOB_NAME | GRADE |
|---|---|---|
| 1 | ASSOCIATE | 1 |
| 2 | CHIEF | 2 |
| 3 | LEADER | 3 |
| 4 | MANAGER | 4 |

### EMPLOYEE

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID（登録時に 10017 から採番） |
| `EMPLOYEE_CODE` | `VARCHAR(8)` | NOT NULL | 社員コード（E + 社員ID） |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 氏名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID（部署なしは NULL） |
| `JOB_ID` | `INT` | 外部キー → `JOB.JOB_ID`、NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社日 |
| `STATUS` | `VARCHAR(10)` | NOT NULL、既定値 'active' | 状態（active / deleted。論理削除） |
| `VERSION` | `INT` | NOT NULL、既定値 0 | バージョン（更新競合の検出） |

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
