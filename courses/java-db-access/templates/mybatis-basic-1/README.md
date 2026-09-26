# mybatis-basic-1 — SqlSession・Mapperインターフェース・CRUD

講座リポジトリ learn_java_db_aidd の `projects/master/07_mybatis-crud/mybatis-basic-1` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/mybatis_basic_1` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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
2. `src/main/resources/mybatis-config.xml` と `mybatis-mapper*.xml`を確認する。
3. 「実行」で main を動かし、SQL・取得結果・更新後のデータを照合する。

## 実行する main（mains.txt の順）

- `pro.kensait.mybatis.company.main1.SelectMain1`
- `pro.kensait.mybatis.company.main1.SelectMain2`
- `pro.kensait.mybatis.company.main1.SelectMain3`
- `pro.kensait.mybatis.company.main1.SelectMain4`
- `pro.kensait.mybatis.company.main1.SelectMain5`
- `pro.kensait.mybatis.company.main1.SelectMain6`
- `pro.kensait.mybatis.company.main1.SelectMain7`
- `pro.kensait.mybatis.company.main1.InsertMain1`
- `pro.kensait.mybatis.company.main1.UpdateMain1`
- `pro.kensait.mybatis.company.main1.UpdateMain2`
- `pro.kensait.mybatis.company.main1.UpdateMain3`
- `pro.kensait.mybatis.company.main1.DeleteMain1`
- `pro.kensait.mybatis.company.main1.DeleteMain2`
- `pro.kensait.mybatis.company.main2.MapperSelectMain1`
- `pro.kensait.mybatis.company.main2.MapperSelectMain2`
- `pro.kensait.mybatis.company.main2.MapperInsertMain1`
- `pro.kensait.mybatis.company.main2.MapperUpdateMain1`
- `pro.kensait.mybatis.company.main2.MapperUpdateMain2`
- `pro.kensait.mybatis.company.main2.MapperDeleteMain1`
