# 41_dbunit: DBUnit によるデータベーステスト

## 何を学ぶ演習か

DBUnit で EMPLOYEE テーブルを CSV の初期データにそろえ、DAO の戻り値と更新後のテーブルを検証します（チャプター4.1）。

## 見るファイル

- `src/main/java/pro/kensait/jdbc/company/EmployeeDAO.java`: テスト対象
- `src/test/java/pro/kensait/jdbc/company/EmployeeDAOTest.java`: `onSetup()` を自分で呼ぶテスト
- `src/test/java/pro/kensait/jdbc/company/EmployeeDAOExtensionTest.java`: `DbUnitExtension` に任せるテスト
- `src/test/java/pro/kensait/jdbc/company/EmployeeDAOFilterSortTest.java`: 列の除外とソート
- `src/test/resources/INIT_DATA/EMPLOYEE.csv`: 初期データ（5 件）
- `src/test/resources/EXPECTED_DATA_3/EMPLOYEE.csv`: 一括更新の期待値

## ボタンでの動かし方

テストクラスを開いて、エディタの右上の ファイル実行を押します。DB はインメモリの HSQLDB で、テーブルはテストの開始時に `src/test/resources/schema.sql` から作られます（`SchemaInitializer`）。サーバーの起動は要りません。

## 結果のどこを見るか

- 「テスト結果」のメソッドごとの成否
- 期待値の CSV を書き換えて流したときの、どの行・どの列の差として報告されるか

## この演習で使うテーブル

EMPLOYEE（EMPLOYEE_ID 主キー、EMPLOYEE_NAME、DEPARTMENT_NAME、ENTRANCE_DATE、JOB_NAME、SALARY）。初期データは Alice・Bob・Carol・Dave・Ellen の 5 件です。
