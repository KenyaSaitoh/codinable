# この演習で使うテーブル

「IdClass と分社エンティティの関連」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## SUBSIDIARY（分社）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `SUBSIDIARY_ID` | `INT` | 主キー | 分社ID |
| `SUBSIDIARY_NAME` | `VARCHAR(30)` | NOT NULL | 分社名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |

初期データ（4 件）

| SUBSIDIARY_ID | SUBSIDIARY_NAME | LOCATION |
|---|---|---|
| 1 | GLOBAL | TOKYO HQ |
| 2 | FUTURE | TOKYO HQ |
| 3 | TECH | YOKOHAMA BRANCH |
| 4 | PRODUCT | YOKOHAMA BRANCH |

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `SUBSIDIARY_ID` | `INT` | 主キー（複合）、外部キー → `SUBSIDIARY.SUBSIDIARY_ID` | 分社ID |
| `EMPLOYEE_ID` | `INT` | 主キー（複合） | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（16 件。先頭 12 件）

| SUBSIDIARY_ID | EMPLOYEE_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 1 | 10001 | Bob | 450000 |
| 1 | 10002 | Frank | 250000 |
| 1 | 10003 | Steve | 380000 |
| 2 | 10001 | Carol | 350000 |
| 2 | 10002 | Justin | 460000 |
| 2 | 10003 | Victor | 220000 |
| 3 | 10001 | Alice | 500000 |
| 3 | 10002 | Dave | 400000 |
| 3 | 10003 | Ellen | 300000 |
| 3 | 10004 | Matilda | 280000 |
| 3 | 10005 | Peggy | 270000 |
| 4 | 10001 | Ivan | 480000 |
