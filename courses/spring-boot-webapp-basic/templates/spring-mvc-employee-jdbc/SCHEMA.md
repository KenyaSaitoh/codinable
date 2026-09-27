# この演習で使うテーブル

「JdbcTemplate による社員管理」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_EMPLOYEE_DDL.sql`、初期データ: `sql/hsqldb/3_EMPLOYEE_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## DEPARTMENT

| カラム | 型 | 制約 |
|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL |
| `LOCATION` | `VARCHAR(50)` | NOT NULL |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION |
|---|---|---|
| 1 | 営業部 | 東京 |
| 2 | 開発部 | 大阪 |
| 3 | 人事部 | 東京 |
| 4 | 総務部 | 福岡 |

## JOB

| カラム | 型 | 制約 |
|---|---|---|
| `JOB_ID` | `INT` | 主キー |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL |
| `GRADE` | `INT` | NOT NULL |

初期データ（5 件）

| JOB_ID | JOB_NAME | GRADE |
|---|---|---|
| 1 | 一般 | 1 |
| 2 | 主任 | 2 |
| 3 | 課長 | 3 |
| 4 | 部長 | 4 |
| 5 | 本部長 | 5 |

## EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 |
| `EMPLOYEE_CODE` | `VARCHAR(8)` | NOT NULL |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID`、NOT NULL |
| `JOB_ID` | `INT` | 外部キー → `JOB.JOB_ID`、NOT NULL |
| `SALARY` | `INT` | NOT NULL |
| `ENTRANCE_DATE` | `DATE` | NOT NULL |
| `STATUS` | `VARCHAR(10)` | NOT NULL、既定値 'active' |
| `VERSION` | `INT` | NOT NULL、既定値 0 |

初期データ（10 件）

| EMPLOYEE_ID | EMPLOYEE_CODE | EMPLOYEE_NAME | DEPARTMENT_ID | JOB_ID | SALARY | ENTRANCE_DATE | STATUS | VERSION |
|---|---|---|---|---|---|---|---|---|
| 1 | E0001 | 山田 太郎 | 1 | 3 | 520000 | 2012-04-01 | active | 0 |
| 2 | E0002 | 佐藤 花子 | 2 | 4 | 680000 | 2010-04-01 | active | 0 |
| 3 | E0003 | 鈴木 一郎 | 3 | 1 | 300000 | 2019-04-01 | active | 0 |
| 4 | E0004 | 高橋 美咲 | 4 | 2 | 380000 | 2017-10-01 | active | 0 |
| 5 | E0005 | 田中 健太 | 1 | 1 | 280000 | 2021-04-01 | active | 0 |
| 6 | E0006 | 伊藤 由美 | 2 | 2 | 450000 | 2016-04-01 | active | 0 |
| 7 | E0007 | 渡辺 翔 | 2 | 1 | 320000 | 2020-10-01 | active | 0 |
| 8 | E0008 | 中村 彩 | 3 | 3 | 500000 | 2014-04-01 | active | 0 |
| 9 | E0009 | 小林 大輔 | 1 | 5 | 700000 | 2008-04-01 | active | 0 |
| 10 | E0010 | 加藤 直樹 | 4 | 1 | 250000 | 2023-04-01 | active | 0 |
