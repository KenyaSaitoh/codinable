# jakarta-tx — @Transactional の伝播と UserTransaction

チャプター 12.1「Jakarta Transactionsによるトランザクション管理と伝播」のサンプルです。

- `FooBean`（`REQUIRED`、`rollbackOn = Exception.class`）が外側の境界を作り、`BarBean` と `QuxBean` を順に呼ぶ
- `BarBean` は `REQUIRES_NEW`（独立したトランザクション）で `BUSINESS` テーブルの `Bar` の行を更新する
- `QuxBean` は `REQUIRED`（外側へ参加）で `Qux` の行を更新し、引数が負なら更新した後に `RuntimeException` を送出する
- `BazBean` は `@Transactional` を付けず、`UserTransaction` で 1 件ずつ `begin`・`commit`／`rollback` する

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/tx/` の下にあります。

| ファイル | 内容 |
|---|---|
| `FooBean.java`、`BarBean.java`、`QuxBean.java` | 伝播の属性（`TxType`）と、`rollbackOn` |
| `FugaServlet.java` | 入口のサーブレット。`/fuga?param=100` の `param` を `FooBean` に渡し、`NORMAL END` か `ERROR OCCURRED!!!` を返す |
| `BazBean.java`、`BazServlet.java` | `UserTransaction` による明示的な制御。`/baz?params=10,-3,20` |
| `DataSourceConfig.java` | `@DataSourceDefinition` で `java:app/jdbc/TxDS`（HSQLDB のインメモリ DB）を定義する |
| `BusinessServlet.java` | 演習用。`/business` で `BUSINESS` テーブルの今の値を表示する |
| `DatabaseInitializer.java`、`sql/hsqldb/` | 演習用。起動時に `BUSINESS` テーブルを作り直す（`COUNT` は 0 から始まる） |
| `curl/01_propagation.sh`、`curl/02_user_transaction.sh` | 要求を送って、どの更新が確定したかを確かめるスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-tx/` が開く
2. 画面から `param` に `100`、続けて `-5` を送り、そのたびに「BUSINESS テーブルの今の値」を開く
3. `curl/01_propagation.sh`、`curl/02_user_transaction.sh` を開いて、エディタの右上の ▶ 実行で流す
   （値は足し込まれていく。▶ アプリ実行でデプロイし直すと 0 に戻る）

## 結果のどこを見るか

| 要求 | `Bar` の `COUNT` | `Qux` の `COUNT` | 応答 |
|---|---|---|---|
| `/fuga?param=100` | 100 | 100 | `NORMAL END` |
| 続けて `/fuga?param=-5` | 95 | 100 | `ERROR OCCURRED!!!` |

- `param=-5` では、`BarBean` の更新は `REQUIRES_NEW` の独立したトランザクションで確定済みなので残り、
  `QuxBean` の更新は `FooBean` のトランザクションとともに取り消される
- 実行結果（Payara のログ）に、`[ FooBean#doBusiness ] Start` → `[ BarBean#doBusiness ] Start` → … の順が出る
- `/baz?params=10,-3,20` は `COMMITTED 2 / 3`。`-3` の 1 件だけが取り消され、`Qux` には 10 と 20 が足される

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「REQUIRED と REQUIRES_NEW の伝播」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_TX_DDL.sql`、初期データ: `sql/hsqldb/3_TX_DML.sql`）。

### BUSINESS

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `NAME` | `VARCHAR(10)` | 主キー | 更新する Bean の名前（Bar / Qux） |
| `COUNT` | `INT` | NOT NULL | param を足し込む値 |

初期データ（2 件）

| NAME | COUNT |
|---|---|
| Bar | 0 |
| Qux | 0 |
<!-- schema:end -->
