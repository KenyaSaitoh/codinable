# この演習で使うテーブル

「@ElementCollection による値のリスト」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` |  | 部署名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（14 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_NAME | SALARY |
|---|---|---|---|
| 10001 | Alice | SALES | 500000 |
| 10002 | Bob | PLANNING | 450000 |
| 10003 | Carol | HR | 350000 |
| 10004 | Dave | SALES | 400000 |
| 10005 | Ellen | SALES | 300000 |
| 10006 | Frank | PLANNING | 250000 |
| 10007 | Ivan | PRODUCT | 480000 |
| 10008 | Justin | HR | 460000 |
| 10009 | Mallory | PRODUCT | 420000 |
| 10010 | Matilda | SALES | 280000 |
| 10011 | Oscar | PRODUCT | 320000 |
| 10012 | Pat | PRODUCT | 240000 |

## EMAIL（メール）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー（複合） | 社員ID |
| `ADDRESS` | `VARCHAR(255)` | 主キー（複合） | メールアドレス |

初期データ（28 件。先頭 12 件）

| EMPLOYEE_ID | ADDRESS |
|---|---|
| 10001 | alice@gmail.com |
| 10001 | alice@outlook.jp |
| 10001 | alice@yahoo.co.jp |
| 10002 | bob@gmail.com |
| 10002 | bob@outlook.jp |
| 10003 | carol@gmail.com |
| 10003 | carol@outlook.jp |
| 10004 | dave@gmail.com |
| 10004 | dave@outlook.jp |
| 10005 | ellen@gmail.com |
| 10006 | frank@gmail.com |
| 10006 | frank@outlook.jp |
