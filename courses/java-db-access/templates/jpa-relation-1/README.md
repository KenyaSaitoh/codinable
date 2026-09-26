# jpa-relation-1 — 1対多／多対1・fetch・cascade

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-relation-1` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_relation_1` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.jpa.company.main.crud.SelectMain1`
- `pro.kensait.jpa.company.main.crud.SelectMain2`
- `pro.kensait.jpa.company.main.crud.InsertMain`
- `pro.kensait.jpa.company.main.crud.MapkeyMain`
- `pro.kensait.jpa.company.main.crud.FetchStrategyMain`
- `pro.kensait.jpa.company.main.crud.CascadeStrategyMain`
- `pro.kensait.jpa.company.main.crud.DeleteMain`
- `pro.kensait.jpa.company.main.lock.CascadeOptimisticLockMain1` と `pro.kensait.jpa.company.main.lock.CascadeOptimisticLockMain2`（同時に実行）

## cascadeの読み方

この教材はcascadeの効果を観察するため、元教材の `CascadeType.ALL` を維持しています。社員側からのremoveが部署や他の社員へ波及し得ます。実務の共有マスタにそのまま適用せず、所有関係と削除要件に応じて伝播する操作を選びます。

## バージョンの確認（CascadeOptimisticLockMain1・2）

元の教材では 2 つのターミナルで同時に動かしていた例です。Codinable では `mains.txt` の最後の行
`CascadeOptimisticLockMain1 & CascadeOptimisticLockMain2` が、1 を起動して 3 秒後に 2 を別スレッドで起動します。
1 は部署とその社員を読み、社員の給与を変えて部署を merge した後 20 秒待ちます。その間に 2 が同じ社員を更新するため、
cascade で社員へ伝わった 1 の更新は古いバージョンとなり、`OptimisticLockException` で終わります
（親を merge しても、カスケードされた子の楽観ロックが効くことを確かめる例です）。
