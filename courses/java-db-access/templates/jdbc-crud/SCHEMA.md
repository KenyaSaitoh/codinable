# この演習で使うテーブル

「PreparedStatement による CRUD と DAO」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/jdbc/EmployeeDaoTest.java`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー |
| `DEPARTMENT_ID` | `INT` | NOT NULL |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `SALARY` | `DECIMAL(12, 2)` | NOT NULL |

初期データはありません（演習のコードが登録します）。
