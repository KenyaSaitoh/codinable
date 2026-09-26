# mybatis-dynamic — foreach・任意条件・部署別集約（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/08_mybatis-dynamic/mybatis-dynamic` を、Codinable で単独で動くようにしたものです。

部署番号・給与下限・社員番号のリストを任意に組み合わせる動的 SQL と、部署別の集約結果を DTO で受け取る例です。

## 読む順番

1. `EmployeeFilter` で、どの条件が任意（null 可）かを確認する。
2. `src/main/resources/pro/kensait/db/mybatisdynamic/CompanyMapper.xml` の `<where>`・`<if>`・`<foreach>` と、
   集約の SELECT（`summarizeDepartments`）を読む。
3. `CompanyMapperTest` で、条件の組み合わせごとに返る社員と、部署ごとの集約結果を確認する。

値は SQL 文字列に連結せずバインドします。社員が 0 人の部署があると、INNER JOIN と LEFT JOIN で集約結果が変わります。

## Codinable での動かし方

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:mybatis_dynamic`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
