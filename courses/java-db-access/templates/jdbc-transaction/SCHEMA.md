# この演習で使うテーブル

「commit・rollback・savepoint・一括更新」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/transaction/PayrollServiceTest.java`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー |
| `DEPARTMENT_ID` | `INT` |  |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `SALARY` | `DECIMAL(12, 2)` | NOT NULL |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | DEPARTMENT_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 10001 | 3 | Alice | 500000.00 |
| 10002 | 1 | Bob | 450000.00 |
| 10003 | 2 | Carol | 350000.00 |
| 10004 | 3 | Dave | 400000.00 |
| 10005 | 3 | Ellen | 300000.00 |
| 10006 | 1 | Frank | 250000.00 |
| 10007 | 4 | Ivan | 480000.00 |
| 10008 | 2 | Justin | 460000.00 |
| 10009 | 4 | Mallory | 420000.00 |
| 10010 | 3 | Matilda | 280000.00 |
| 10011 | 4 | Oscar | 320000.00 |
| 10012 | 4 | Pat | 240000.00 |
