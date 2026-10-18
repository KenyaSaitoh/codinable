# jakarta-tx-xa — 2 つの XA データソースによる分散トランザクション

チャプター 12.1「Jakarta Transactionsによるトランザクション管理と伝播」のうち、分散トランザクションのサンプルです。
`JtaServlet` に `@DataSourceDefinitions` で XA 対応のデータソース `TxDS1`・`TxDS2` を定義し、
`BarBean` は `TxDS1`、`QuxBean` は `TxDS2` を通じて `BUSINESS_XA` テーブルを更新します。
どちらも `REQUIRED` なので、`FooBean` が始めた 1 つのトランザクションに 2 つの XA リソースとして参加し、
Payara Server が内蔵するトランザクションマネージャーが 2 フェーズコミットで確定・取り消しをそろえます。

演習では、`TxDS1` と `TxDS2` は同じインメモリの HSQLDB（`org.hsqldb.jdbc.pool.JDBCXADataSource`）を指す、論理的に別の XA リソースです。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/tx/` の下にあります。

| ファイル | 内容 |
|---|---|
| `JtaServlet.java` | `@DataSourceDefinitions` で 2 つの XA データソースを定義する。`/jta?param=100` の入口 |
| `FooBean.java` | 外側の境界（`REQUIRED`、`rollbackOn = Exception.class`） |
| `BarBean.java`、`QuxBean.java` | それぞれ `TxDS1`・`TxDS2` を `@Resource` で注入して更新する。`QuxBean` は引数が負なら更新後に例外を送出する |
| `BusinessServlet.java` | 演習用。`/business` で `BUSINESS_XA` テーブルの今の値を表示する |
| `DatabaseInitializer.java`、`sql/hsqldb/` | 演習用。起動時に `BUSINESS_XA` テーブルを作り直す |
| `curl/01_xa.sh` | 要求を送って、どの更新が確定したかを確かめるスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-tx-xa/` が開く
2. 画面から `param` に `100`、続けて `-5` を送り、そのたびに「BUSINESS_XA テーブルの今の値」を開く
3. `curl/01_xa.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

| 要求 | `Bar` の `COUNT`（TxDS1） | `Qux` の `COUNT`（TxDS2） | 応答 |
|---|---|---|---|
| `/jta?param=100` | 100 | 100 | `NORMAL END` |
| 続けて `/jta?param=-5` | 100 | 100 | `ERROR OCCURRED!!!` |

- `param=-5` では、`QuxBean` の例外で、別のデータソースを通じた `Bar` の更新も含めて全体が取り消される
  （`jakarta-tx` では `BarBean` が `REQUIRES_NEW` だったので `Bar` だけが残った）

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「2 つの XA データソースによる分散トランザクション」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_TX_XA_DDL.sql`、初期データ: `sql/hsqldb/3_TX_XA_DML.sql`）。

### BUSINESS_XA

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
