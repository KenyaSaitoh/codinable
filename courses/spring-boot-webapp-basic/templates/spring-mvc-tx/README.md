# spring-mvc-tx — @Transactional の伝播（REQUIRED / REQUIRES_NEW）

チャプター 10.1「Springのトランザクション管理と伝播」で使うサンプルです。

- `FooBean`（`REQUIRED`、`rollbackFor = Exception.class`）が外側の境界を作り、
  `BarBean` と `QuxBean` を順に呼ぶ
- `BarBean` は `REQUIRES_NEW`（独立したトランザクション）で `BUSINESS` 表の `Bar` 行を更新する
- `QuxBean` は `REQUIRED`（外側へ参加）で `Qux` 行を更新し、引数が負なら更新後に `RuntimeException` を送出する

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. `http://localhost:8080/fuga?param=100` を開くと `NORMAL END`
3. `http://localhost:8080/fuga?param=-5` を開くと `ERROR OCCURED!!!`
4. 実行ログで `[ BarBean#doBusiness ]` と `[ QuxBean#doBusiness ]` の Start / End の順を確かめる

DB は Codinable 用にインメモリの HSQLDB（`jdbc:hsqldb:mem:testdb`）へ切り替えてあり、
起動時に `sql/hsqldb` の SQL で `BUSINESS` 表を作ります（HSQLDB サーバーは要りません）。
`src/main/webapp` の `TxPage.html` などはサンプルに含まれていた旧構成の名残で、Spring Boot の実行では使いません。
