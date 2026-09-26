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

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:mybatis_crud`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
