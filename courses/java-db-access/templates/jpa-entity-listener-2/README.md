# jpa-entity-listener-2 — コールバックの責務と明示的な残高更新

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-entity-listener-2` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_entity_listener_2` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.jpa.main.AccountBalanceMain`

## 取り込み時の補完

元教材は銀行口座のモデルに対してEmployee用のpersistence.xmlを持ち、初期データとmainがありませんでした。実エンティティの登録、最小データ、実行例を補っています。
元の `@PostLoad` は別のEntityManagerFactoryを生成して残高を書き換えていました。取得時の意図しない更新を避けるため、通知は `AccountListener`、未反映明細の加減算は `AccountBalanceService` に配置しました。これは責務の学習用の最小例で、同時更新を扱う実用の銀行システムではありません。
