# jdbc-transaction — commit・rollback・savepoint・batch・CallableStatement

講座リポジトリ learn_java_db_aidd の `projects/master/04_jdbc-transaction/jdbc-transaction` を、Codinable で単独で動くようにしたものです。

更新処理では `autoCommit=false` とし、成功時だけ commit、失敗時は rollback します。
複数行更新の `addBatch` / `executeBatch`、部分的に戻す savepoint、ストアドプロシージャを呼ぶ `CallableStatement` も扱います。

## 読む順番

`PayrollService` と `PayrollServiceTest` を対応づけて読みます。

- 一括昇給: 全社員を更新できた場合だけ commit する。途中の制約違反や、存在しない社員への更新 0 件は全体を rollback する。
- savepoint: 名前の変更後に savepoint を置き、給与だけ取り消して名前は確定する。給与更新自体が失敗した場合は名前も取り消す。
- CallableStatement: `registerOutParameter` で登録した OUT 引数から部署の社員数を受け取る。プロシージャの DDL は HSQLDB 固有。

batch はトランザクションそのものではありません。成功時でもドライバが更新件数を返せず `SUCCESS_NO_INFO` を返す場合があります。
例外時の rollback がさらに失敗した場合は、元の例外の suppressed 例外として保持します。取得した接続は Service が所有して閉じます。

## Codinable での動かし方

- テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。
- `PayrollServiceTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:transaction_course`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「commit・rollback・savepoint・一括更新」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/transaction/PayrollServiceTest.java`）。

### EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー |
| `DEPARTMENT_ID` | `INT` |  |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `SALARY` | `DECIMAL(12, 2)` | NOT NULL |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | DEPARTMENT_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 10001 | 3 | Alice | 500000.00 |
| 10002 | 1 | Bob | 450000.00 |
| 10003 | 2 | Carol | 350000.00 |
| 10004 | 3 | Dave | 400000.00 |
| 10005 | 3 | Ellen | 300000.00 |
| 10006 | 1 | Frank | 250000.00 |
| 10007 | 4 | Ivan | 480000.00 |
| 10008 | 2 | Justin | 460000.00 |
| 10009 | 4 | Mallory | 420000.00 |
| 10010 | 3 | Matilda | 280000.00 |
| 10011 | 4 | Oscar | 320000.00 |
| 10012 | 4 | Pat | 240000.00 |
<!-- schema:end -->
