# jdbc-connection — JDBC URL と Connection の取得

講座リポジトリ learn_java_db_aidd の `projects/master/02_jdbc-connection/jdbc-connection` を、Codinable で単独で動くようにしたものです。

JDBC URL `jdbc:hsqldb:mem:connection_course` を、`jdbc:<subprotocol>:<subname>` の 3 要素として読み解きます。
`DriverManager` は小規模な単発接続、`DataSource` は設定と接続取得を分離し、プールや JNDI へ発展できる入口です。

## 読む順番

`JdbcUrl` → `ConnectionFactory` → `ConnectionFactoryTest` の順に読みます。
テストでは接続先 DB の製品名とトランザクション対応を `DatabaseMetaData`、初期の autoCommit を
`Connection#getAutoCommit()` で確認します。不完全な URL を拒否することも確認します。

`DataSource` というインターフェース自体が接続プールを保証するわけではありません。この例の `JDBCDataSource` はプールなしです。
接続は呼び出し元が try-with-resources で閉じます。JDBC ドライバはサービスプロバイダ機構で登録されるため、`Class.forName` は不要です。
`mem:`（同じ JVM の中だけの DB）と `hsql://`（サーバーへの接続）の違いも、URL の subname に表れます。

## Codinable での動かし方

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:connection_course`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
