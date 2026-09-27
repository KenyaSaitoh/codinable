# この演習で使うテーブル

「JPQL の検索・結合・集約」・「一括更新と flush モード」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |
| `VERSION` | `INT` |  | バージョン |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION | VERSION |
|---|---|---|---|
| 1 | PLANNING | TOKYO HQ | 0 |
| 2 | HR | TOKYO HQ | 0 |
| 3 | SALES | YOKOHAMA BRANCH | 0 |
| 4 | PRODUCT | YOKOHAMA BRANCH | 0 |

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` |  | 部署ID |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社年月日 |
| `JOB_ID` | `INT` | NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `VERSION` | `INT` |  | バージョン |

初期データ（14 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | ENTRANCE_DATE | JOB_ID | SALARY | VERSION |
|---|---|---|---|---|---|---|
| 10001 | Alice | 3 | 2012-04-01 | 0 | 500000 | 0 |
| 10002 | Bob | 1 | 2012-04-01 | 0 | 450000 | 0 |
| 10003 | Carol | 2 | 2012-04-01 | 2 | 350000 | 0 |
| 10004 | Dave | 3 | 2012-04-01 | 1 | 400000 | 0 |
| 10005 | Ellen | 3 | 2013-04-01 | 2 | 300000 | 0 |
| 10006 | Frank | 1 | 2013-10-01 | 3 | 250000 | 0 |
| 10007 | Ivan | 4 | 2014-01-01 | 0 | 480000 | 0 |
| 10008 | Justin | 2 | 2014-04-01 | 0 | 460000 | 0 |
| 10009 | Mallory | 4 | 2014-07-01 | 1 | 420000 | 0 |
| 10010 | Matilda | 3 | 2015-08-01 | 3 | 280000 | 0 |
| 10011 | Oscar | 4 | 2015-11-01 | 2 | 320000 | 0 |
| 10012 | Pat | 4 | 2016-04-01 | 3 | 240000 | 0 |
