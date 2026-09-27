# この演習で使うテーブル

「読み込み通知と残高更新の責務の分離」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_TRAN_DDL.sql`、初期データ: `sql/hsqldb/3_TRAN_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## CIF

| カラム | 型 | 制約 |
|---|---|---|
| `CIF_NUM` | `INT` | 主キー |
| `CUSTOMER_NAME` | `VARCHAR(20)` | NOT NULL |

初期データ（1 件）

| CIF_NUM | CUSTOMER_NAME |
|---|---|
| 1 | Alice |

## ACCOUNT

| カラム | 型 | 制約 |
|---|---|---|
| `BRANCH_NUM` | `INT` | 主キー（複合） |
| `ACCOUNT_NUM` | `INT` | 主キー（複合） |
| `CIF_NUM` | `INT` | NOT NULL |
| `BALANCE` | `DECIMAL(13)` | NOT NULL |
| `LAST_TRAN_DATE` | `DATE` | NOT NULL |
| `LAST_TRAN_NUM` | `INT` | NOT NULL |

初期データ（1 件）

| BRANCH_NUM | ACCOUNT_NUM | CIF_NUM | BALANCE | LAST_TRAN_DATE | LAST_TRAN_NUM |
|---|---|---|---|---|---|
| 101 | 10001 | 1 | 200000 | 2026-01-01 | 0 |

## TRAN_DETAIL

| カラム | 型 | 制約 |
|---|---|---|
| `BRANCH_NUM` | `INT` | 主キー（複合） |
| `ACCOUNT_NUM` | `INT` | 主キー（複合） |
| `TRAN_DATE` | `DATE` | 主キー（複合） |
| `TRAN_NUM` | `INT` | 主キー（複合） |
| `PAY_REC_TYPE` | `VARCHAR(3)` | NOT NULL |
| `AMOUNT` | `DECIMAL(13)` | NOT NULL |

初期データ（2 件）

| BRANCH_NUM | ACCOUNT_NUM | TRAN_DATE | TRAN_NUM | PAY_REC_TYPE | AMOUNT |
|---|---|---|---|---|---|
| 101 | 10001 | 2026-01-01 | 1 | REC | 10000 |
| 101 | 10001 | 2026-01-02 | 1 | PAY | 5000 |
