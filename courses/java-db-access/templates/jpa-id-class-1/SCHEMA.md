# この演習で使うテーブル

「@IdClass による複合主キー」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `SUBSIDIARY_NAME` | `VARCHAR(30)` | 主キー（複合） | 分社名 |
| `EMPLOYEE_ID` | `INT` | 主キー（複合） | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（16 件。先頭 12 件）

| SUBSIDIARY_NAME | EMPLOYEE_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| FUTURE | 10001 | Carol | 350000 |
| FUTURE | 10002 | Justin | 460000 |
| FUTURE | 10003 | Victor | 220000 |
| GLOBAL | 10001 | Bob | 450000 |
| GLOBAL | 10002 | Frank | 250000 |
| GLOBAL | 10003 | Steve | 380000 |
| PRODUCT | 10001 | Ivan | 480000 |
| PRODUCT | 10002 | Mallory | 420000 |
| PRODUCT | 10003 | Oscar | 320000 |
| PRODUCT | 10004 | Pat | 240000 |
| PRODUCT | 10005 | Trent | 310000 |
| TECH | 10001 | Alice | 500000 |
