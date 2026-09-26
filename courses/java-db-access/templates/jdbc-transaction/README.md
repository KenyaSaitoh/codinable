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

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:transaction_course`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
