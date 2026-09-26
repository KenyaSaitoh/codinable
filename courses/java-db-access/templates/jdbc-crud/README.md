# jdbc-crud — Statement・PreparedStatement・ResultSet による CRUD

講座リポジトリ learn_java_db_aidd の `projects/master/03_jdbc-crud/jdbc-crud` を、Codinable で単独で動くようにしたものです。

DDL の実行には `Statement`、値を受け取る CRUD には、SQL インジェクションを防ぎ型変換を明示できる `PreparedStatement` を使います。
`ResultSet` から `Employee` へのマッピングも DAO の中に閉じ込めます。

## 読む順番

`Employee` → `EmployeeDao` → `EmployeeDaoTest` の順に読みます。

この例では DAO の 1 回の更新を 1 トランザクションとして確定し、検索は別の接続で DB から取り直します。
`StatementBinder` は値の設定部分を共通の実行処理に渡すための小さな関数型インターフェースです。

テストでは CRUD、該当なしの Optional／更新件数 0、重複キーの例外、引用符や SQL 構文を含む名前が値として保存されることを確認します。
DDL による削除・再作成はテストの初期化に限っています。
PreparedStatement が守るのはバインドした値であり、文字列連結したテーブル名や ORDER BY の列名まで安全になるわけではありません。

## Codinable での動かし方

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:crud_course`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
