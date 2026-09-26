# spring-mvc-employee-mybatis — MyBatis による社員管理

チャプター 9.1「MyBatisによるSQLマッピングと動的SQL」で使うサンプルです。
Mapper インタフェース（`EmployeeMapper` など）と、同じパッケージ階層の XML
（`src/main/resources/pro/kensait/spring/employee/mapper/*.xml`）の SQL が対応します。
一覧と件数取得は共通の条件（`activeConditions`）を使い、`<if>` による動的 SQL で組み立てます。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開く
3. 条件を変えて検索し、一覧と総件数・ページ数がそろうことを確かめる
4. 登録・編集・削除を操作する

DB は Codinable 用にインメモリの HSQLDB（`jdbc:hsqldb:mem:testdb`）へ切り替えてあります。
起動時に `sql/hsqldb/2_EMPLOYEE_DDL.sql` と `3_EMPLOYEE_DML.sql` を流すので、
HSQLDB サーバーの起動や初期化は要りません。登録・更新した内容は停止すると消えます。
`sql/hsqldb` の SQL は、実行対象で `sql:` を選べば Codinable の SQL 実行でも中身を確かめられます。
