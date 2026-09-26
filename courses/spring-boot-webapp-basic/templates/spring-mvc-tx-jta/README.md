# spring-mvc-tx-jta — JTA による複数データソースのグローバルトランザクション

チャプター 10.1「Springのトランザクション管理と伝播」（10.1.5 ローカルトランザクションとJTA）で使うサンプルです。

- `XaDataSourceConfig` が HSQLDB の XA データソースを 2 つ（`dataSource1` / `dataSource2`）、
  Atomikos の `AtomikosDataSourceBean` として登録する
- `BarBean` は `dataSource1`、`QuxBean` は `dataSource2` を `@Qualifier` で選び、
  どちらも `jakarta.transaction.Transactional`（`REQUIRED`）で `FooBean` のグローバルトランザクションに参加する
- `QuxBean` が失敗すると、`BarBean` の更新も含めて全体がロールバックされる

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. `http://localhost:8080/JtaPage.html` を開き、PARAMETER に 100 や -5 を入れて送る
   （`http://localhost:8080/jta?param=100` を直接開いてもよい）
3. 正常時は `NORMAL END`、負の値では `ERROR OCCURED!!!` が返ることと、実行ログの流れを確かめる

## Codinable 向けに変えた点

- DB は HSQLDB サーバーではなく、同じ JVM 内のインメモリ DB（`jdbc:hsqldb:mem:testdb`）。
  サンプルも「同じ testdb を 2 つのデータソースとして登録する」構成なので、2 つの XA リソースが
  1 つのグローバルトランザクションに参加する点は同じ。起動時に `sql/hsqldb` の SQL で表を作る
- URL に `hsqldb.tx=mvcc` を付けた。HSQLDB 既定のテーブル単位のロックでは、
  2 つ目の接続が 1 つ目の更新の終わりを待ち続け、タイムアウトまで応答が返らないため
- Atomikos のスターターを `transactions-spring-boot3-starter:6.0.0` から
  Spring Boot 4 用の `transactions-spring-boot4-starter:6.0.1` に替えた（前者は Spring Boot 4 で起動できない）
- Atomikos のトランザクションログは `build/atomikos` に出す
