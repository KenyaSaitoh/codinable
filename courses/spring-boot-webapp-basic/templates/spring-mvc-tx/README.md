# spring-mvc-tx — @Transactional の伝播（REQUIRED / REQUIRES_NEW）

チャプター 10.1「Springのトランザクション管理と伝播」で使うサンプルです。

- `FooBean`（`REQUIRED`、`rollbackFor = Exception.class`）が外側の境界を作り、
  `BarBean` と `QuxBean` を順に呼ぶ
- `BarBean` は `REQUIRES_NEW`（独立したトランザクション）で `BUSINESS` 表の `Bar` 行を更新する
- `QuxBean` は `REQUIRED`（外側へ参加）で `Qux` 行を更新し、引数が負なら更新後に `RuntimeException` を送出する

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. `http://localhost:8080/fuga?param=100` を開くと `NORMAL END`
3. `http://localhost:8080/fuga?param=-5` を開くと `ERROR OCCURED!!!`
4. 実行ログで `[ BarBean#doBusiness ]` と `[ QuxBean#doBusiness ]` の Start / End の順を確かめる

DB は Codinable 用にインメモリの HSQLDB（`jdbc:hsqldb:mem:testdb`）へ切り替えてあり、
起動時に `sql/hsqldb` の SQL で `BUSINESS` 表を作ります（HSQLDB サーバーは要りません）。
`src/main/webapp` の `TxPage.html` などはサンプルに含まれていた旧構成の名残で、Spring Boot の実行では使いません。

## 主なファイル

- `src/main/java/pro/kensait/spring/tx/FooBean.java` … 外側の境界（REQUIRED）
- `src/main/java/pro/kensait/spring/tx/BarBean.java` … REQUIRES_NEW
- `src/main/java/pro/kensait/spring/tx/QuxBean.java` … REQUIRED と例外
- `src/main/java/pro/kensait/spring/tx/FugaController.java` … /fuga

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「REQUIRED と REQUIRES_NEW の伝播」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/1_SPRING_TX_DDL.sql`、初期データ: `sql/hsqldb/2_SPRING_TX_DML.sql`）。

### BUSINESS

| カラム | 型 | 制約 |
|---|---|---|
| `NAME` | `VARCHAR(10)` | 主キー |
| `COUNT` | `INT` | NOT NULL |

初期データ（2 件）

| NAME | COUNT |
|---|---|
| Bar | 0 |
| Qux | 0 |
<!-- schema:end -->
