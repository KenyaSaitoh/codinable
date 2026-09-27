# この演習で使うテーブル

「SINGLE_TABLE による継承」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## DEPARTMENT（部署）

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

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID |
| `EMPLOYEE_TYPE` | `CHAR(1)` | NOT NULL | 社員種別 |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社年月日 |
| `JOB_NAME` | `VARCHAR(30)` |  | 役職名 |
| `SALARY` | `INT` |  | 月給（社員） |
| `PARTTIMER_PAYMENT` | `INT` |  | 時給（パート） |

初期データ（14 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | EMPLOYEE_TYPE | ENTRANCE_DATE | JOB_NAME | SALARY | PARTTIMER_PAYMENT |
|---|---|---|---|---|---|---|---|
| 10001 | Alice | 3 | 1 | 2012-04-01 | マネージャ | 500000 | *(NULL)* |
| 10002 | Bob | 1 | 1 | 2012-04-01 | マネージャ | 450000 | *(NULL)* |
| 10003 | Carol | 2 | 1 | 2012-04-01 | チーフ | 350000 | *(NULL)* |
| 10004 | Dave | 3 | 1 | 2012-04-01 | リーダー | 400000 | *(NULL)* |
| 10005 | Ellen | 3 | 1 | 2013-04-01 | チーフ | 300000 | *(NULL)* |
| 10006 | Frank | 1 | 1 | 2013-10-01 | アソシエイト | 250000 | *(NULL)* |
| 10007 | Ivan | 4 | 1 | 2014-01-01 | マネージャ | 480000 | *(NULL)* |
| 10008 | Justin | 2 | 1 | 2014-04-01 | マネージャ | 460000 | *(NULL)* |
| 10009 | Mallory | 4 | 1 | 2014-07-01 | リーダー | 420000 | *(NULL)* |
| 10010 | Matilda | 3 | 1 | 2015-08-01 | アソシエイト | 280000 | *(NULL)* |
| 10011 | Oscar | 4 | 1 | 2015-11-01 | チーフ | 320000 | *(NULL)* |
| 10012 | Pat | 4 | 2 | 2016-04-01 | *(NULL)* | *(NULL)* | 2000 |
