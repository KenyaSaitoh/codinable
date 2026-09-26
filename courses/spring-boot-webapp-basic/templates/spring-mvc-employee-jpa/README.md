# spring-mvc-employee-jpa — Spring Data JPA による社員管理

チャプター 8.1「Spring Data JPAと永続化コンテキスト」で使うサンプルです。
JDBC 版と同じ画面・URL のまま、永続化を `JpaRepository` と JPQL、`Pageable` に置き換えています。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開く
3. 検索・ページ送り・登録・編集・削除を操作し、JDBC 版と同じ動きになることを確かめる
4. 実行ログで Hibernate が発行する SQL の流れを確かめる

DB は Codinable 用にインメモリの HSQLDB（`jdbc:hsqldb:mem:testdb`）へ切り替えてあります。
起動時に `sql/hsqldb/2_EMPLOYEE_DDL.sql` と `3_EMPLOYEE_DML.sql` を流すので、
HSQLDB サーバーの起動や初期化は要りません。登録・更新した内容は停止すると消えます。
`sql/hsqldb` の SQL は、実行対象で `sql:` を選べば Codinable の SQL 実行でも中身を確かめられます。
テーブルは SQL で作るので、`spring.jpa.hibernate.ddl-auto` は `none` のままです。
