# jpa-crud — EntityManager と Repository（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/05_jpa-crud/jpa-crud` を、Codinable で単独で動くようにしたものです。

`EntityManagerFactory` は共有する生成器、`EntityManager` は処理単位の永続化コンテキストです。
managed なエンティティの変更は flush 時に SQL へ反映されます。

## 読む順番

1. `src/main/resources/META-INF/persistence.xml` で永続化ユニットと接続先を確認する。
2. `Employee`（エンティティ）と `EmployeeRepository`（EntityManager を引数で受け取る）を読む。
3. `EmployeeRepositoryTest` で、トランザクションを呼び出し元が開始・確定する分担を確認する。

`@Version` の値が増えるだけでなく、古いバージョンでの更新が拒否されることは `jpa-lock` のテストで確認します。

## Codinable での動かし方

- 実行対象で **Gradle: test** を選び、「実行」を押します。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:jpa_crud`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。
