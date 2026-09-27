# この演習で使うテーブル

「部署別の集約を DTO で受け取る」・「foreach と集合条件」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/mybatisdynamic/CompanyMapperTest.java`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## DEPARTMENT

| カラム | 型 | 制約 |
|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー |
| `DEPARTMENT_NAME` | `VARCHAR(100)` | NOT NULL |

初期データ（2 件）

| DEPARTMENT_ID | DEPARTMENT_NAME |
|---|---|
| 10 | 営業部 |
| 20 | 開発部 |

## EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID`、NOT NULL |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `SALARY` | `DECIMAL(12, 2)` | NOT NULL |

初期データ（3 件）

| EMPLOYEE_ID | DEPARTMENT_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 101 | 10 | 佐藤 花子 | 420000.00 |
| 102 | 10 | 鈴木 一郎 | 380000.00 |
| 201 | 20 | 田中 次郎 | 520000.00 |
