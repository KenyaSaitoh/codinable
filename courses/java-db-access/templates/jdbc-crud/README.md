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

- テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。
- `EmployeeDaoTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:crud_course`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「PreparedStatement による CRUD と DAO」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/jdbc/EmployeeDaoTest.java`）。

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
