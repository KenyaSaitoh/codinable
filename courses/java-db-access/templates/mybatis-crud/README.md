# mybatis-crud — Mapper による基本の CRUD（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/07_mybatis-crud/mybatis-crud` を、Codinable で単独で動くようにしたものです。

`mybatis-config.xml`、Mapper インターフェース `EmployeeMapper`、Mapper XML `EmployeeMapper.xml` の対応を、
テストを入口に確認する小さな教材です。

## 読む順番

1. `src/main/resources/mybatis-config.xml` で接続先と Mapper XML の登録を確認する。
2. `EmployeeMapper.java` と `src/main/resources/pro/kensait/db/mybatis/EmployeeMapper.xml` のメソッドと SQL-ID を対応させる。
3. `EmployeeMapperTest` で、登録・検索・更新・削除と commit の関係を読む。

`MyBatisFactory` が設定ファイルから `SqlSessionFactory` を作り、Session は処理ごとに開いて閉じます。MyBatis に JPA のような変更検知はありません。
`#{}` は値のバインド、`${}` は文字列置換です。

## Codinable での動かし方

- テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。
- `EmployeeMapperTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:mybatis_crud`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「Mapper の CRUD をテストで確かめる」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/mybatis/EmployeeMapperTest.java`）。

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
