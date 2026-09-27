# この演習で使うテーブル

「@Embeddable による値オブジェクト」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | ビル名 |
| `POSTAL_CODE` | `VARCHAR(10)` |  | 郵便番号 |
| `TOWN` | `VARCHAR(10)` |  | 市町村 |
| `STREET` | `VARCHAR(20)` |  | 番地 |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION | POSTAL_CODE | TOWN | STREET |
|---|---|---|---|---|---|
| 1 | PLANNING | TOKYO HQ | 100-0000 | 東京都千代田区 | 1-2-3 |
| 2 | HR | TOKYO HQ | 100-0000 | 東京都千代田区 | 1-2-3 |
| 3 | SALES | YOKOHAMA BRANCH | 160-0001 | 東京都新宿区 | 1-2-3 |
| 4 | PRODUCT | YOKOHAMA BRANCH | 160-0001 | 東京都新宿区 | 1-2-3 |

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID |
| `JOB_ID` | `INT` | NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `ZIP_CODE` | `VARCHAR(10)` |  | 郵便番号 |
| `CITY` | `VARCHAR(10)` |  | 市町村 |
| `STREET` | `VARCHAR(20)` |  | 番地 |

初期データ（14 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | JOB_ID | SALARY | ZIP_CODE | CITY | STREET |
|---|---|---|---|---|---|---|---|
| 10001 | Alice | 3 | 0 | 500000 | 132-0000 | 東京都江戸川区 | 1-2-3 |
| 10002 | Bob | 1 | 0 | 450000 | 279-0000 | 千葉県浦安市 | 1-2-3 |
| 10003 | Carol | 2 | 2 | 350000 | 270-1300 | 千葉県印西市 | 1-2-3 |
| 10004 | Dave | 3 | 1 | 400000 | 221-0000 | 神奈川県横浜市 | 1-2-3 |
| 10005 | Ellen | 3 | 2 | 300000 | 184-0000 | 東京都小金井市 | 1-2-3 |
| 10006 | Frank | 1 | 3 | 250000 | *(NULL)* | *(NULL)* | *(NULL)* |
| 10007 | Ivan | 4 | 0 | 480000 | 182-0000 | 東京都調布市 | 1-2-3 |
| 10008 | Justin | 2 | 0 | 460000 | 220-0000 | 神奈川県横浜市 | 1-2-3 |
| 10009 | Mallory | 4 | 1 | 420000 | 273-0000 | 千葉県船橋市 | 1-2-3 |
| 10010 | Matilda | 3 | 3 | 280000 | *(NULL)* | *(NULL)* | *(NULL)* |
| 10011 | Oscar | 4 | 2 | 320000 | 340-0000 | 埼玉県草加市 | 1-2-3 |
| 10012 | Pat | 4 | 3 | 240000 | 210-0000 | 神奈川県川崎市 | 1-2-3 |
