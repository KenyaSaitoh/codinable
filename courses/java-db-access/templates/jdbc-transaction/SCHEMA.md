# この演習で使うテーブル

「commit・rollback・savepoint・一括更新」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/transaction/PayrollServiceTest.java`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー |
| `DEPARTMENT_ID` | `INT` | NOT NULL |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `SALARY` | `DECIMAL(12, 2)` | NOT NULL |

初期データ（2 件）

| EMPLOYEE_ID | DEPARTMENT_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 101 | 10 | 佐藤 花子 | 400000.00 |
| 102 | 10 | 鈴木 一郎 | 380000.00 |
