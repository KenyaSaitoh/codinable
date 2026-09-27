# この演習で使うテーブル

「where・if による任意条件の検索」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` |  | 部署名 |
| `ENTRANCE_DATE` | `DATE` |  | 入社年月日 |
| `JOB_NAME` | `VARCHAR(30)` |  | 役職名 |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `VERSION` | `INT` |  | バージョン |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_NAME | ENTRANCE_DATE | JOB_NAME | SALARY | VERSION |
|---|---|---|---|---|---|---|
| 10001 | Alice | SALES | 2012-04-01 | MANAGER | 500000 | 0 |
| 10002 | Bob | PLANNING | 2012-04-01 | MANAGER | 450000 | 0 |
| 10003 | Carol | HR | 2012-04-01 | CHIEF | 350000 | 0 |
| 10004 | Dave | SALES | 2012-04-01 | LEADER | 400000 | 0 |
| 10005 | Ellen | SALES | 2013-04-01 | CHIEF | 300000 | 0 |
| 10006 | Frank | PLANNING | 2013-10-01 | ASSOCIATE | 250000 | 0 |
| 10007 | Ivan | PRODUCT | 2014-01-01 | MANAGER | 480000 | 0 |
| 10008 | Justin | HR | 2014-04-01 | MANAGER | 460000 | 0 |
| 10009 | Mallory | PRODUCT | 2014-07-01 | LEADER | 420000 | 0 |
| 10010 | Matilda | SALES | 2015-08-01 | ASSOCIATE | 280000 | 0 |
| 10011 | Oscar | PRODUCT | 2015-11-01 | CHIEF | 320000 | 0 |
| 10012 | Pat | PRODUCT | 2016-04-01 | ASSOCIATE | 240000 | 0 |
