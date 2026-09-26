# jpa-jpql-query — JPQLの検索・結合・集約・一括更新

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-jpql-query` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_jpql_query` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

## Codinable での動かし方

- 実行対象で **Gradle: run** を選び、「実行」を押します。`mains.txt` に並べた main を上から順に実行し、
  出力は `===== [番号/件数] クラス名 =====` で区切られます。
- main ごとに `sql/hsqldb/*.sql`（テーブルの削除・作成・初期データ）を流し直すので、どの main も初期データから始まります。
  DB は HSQLDB のインメモリ（`jdbc:hsqldb:mem:testdb`）で、元の教材の HSQLDB サーバーや `setupHsqldb` は使いません。
- **Gradle: test** では、マッピングと各 main の実行をテストで確かめます。
- 1 つだけ動かしたいときは、`mains.txt` のほかの行の先頭に `#` を付けてから「実行」します。
  ターミナルからは `.\gradlew.bat run -PmainClass=完全修飾クラス名` でも動かせます
  （`gradlew.bat` は一度「実行」を押すとプロジェクト直下に作られます）。
- 起動クラス `pro.kensait.course.SampleLauncher` と `mains.txt` は Codinable 用に足したものです。教材の main は元のままです。

## 読む順番

1. `sql/hsqldb/2_*.sql` と `3_*.sql` でテーブル・初期データを確認する。
2. `src/main/resources/META-INF/persistence.xml` とエンティティの注釈を確認する。
3. 「実行」で main を動かし、SQL・取得結果・更新後のデータを照合する。

## 実行する main（mains.txt の順）

- `pro.kensait.jpa.company.main.JpaQueryMain`
- `pro.kensait.jpa.company.main.JpaTypedQueryMain`
- `pro.kensait.jpa.company.main.JpaWhereClauseMain`
- `pro.kensait.jpa.company.main.JpaWhereInMain`
- `pro.kensait.jpa.company.main.JpaLiteralMain`
- `pro.kensait.jpa.company.main.JpaGeneralFunctionMain`
- `pro.kensait.jpa.company.main.JpaPathExpressionMain`
- `pro.kensait.jpa.company.main.JpaInnerJoinMain`
- `pro.kensait.jpa.company.main.JpaOuterJoinMain`
- `pro.kensait.jpa.company.main.JpaFetchJoinMain`
- `pro.kensait.jpa.company.main.JpaAggregateFunctionMain`
- `pro.kensait.jpa.company.main.JpaGroupByMain`
- `pro.kensait.jpa.company.main.JpaConstructorMain`
- `pro.kensait.jpa.company.main.JpaOrderByMain`
- `pro.kensait.jpa.company.main.JpaDistinctMain`
- `pro.kensait.jpa.company.main.JpaSubQueryMain`
- `pro.kensait.jpa.company.main.JpaNamedQueryMain`
- `pro.kensait.jpa.company.main.JpaBulkUpdateMain1`
- `pro.kensait.jpa.company.main.JpaBulkDeleteMain`
- `pro.kensait.jpa.company.main.JpaBulkUpdateMain2`
- `pro.kensait.jpa.company.main.JpaFlashModeMain`

## クエリの注意点

JPQLはテーブル名ではなくエンティティ名・属性名を使用します。`LocalDate` の条件には `LocalDate` をバインドします。文字列で属性名を指定するCriteria APIも、属性名の誤りまでコンパイル時に検出できるわけではありません。一括UPDATE/DELETEは永続化コンテキスト内の既存オブジェクトを自動更新しないため、clearやrefreshの要否を確認します。
