# mybatis-lock — 更新件数で検出する楽観ロックとFOR UPDATE

講座リポジトリ learn_java_db_aidd の `projects/master/07_mybatis-crud/mybatis-lock` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/mybatis_lock` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.mybatis.company.main.OptimisticLockMain1` と `pro.kensait.mybatis.company.main.OptimisticLockMain2`（同時に実行）
- `pro.kensait.mybatis.company.main.PessimisticLockMain1` と `pro.kensait.mybatis.company.main.PessimisticLockMain2`（同時に実行）

## ロックの確認

元の教材では、HSQLDB サーバーに 2 つのターミナルから接続し、最初のプログラムが sleep に入った後に 2 つ目を起動していました。
Codinable では `mains.txt` の `A & B` の行がこれを 1 回の実行で再現します。A を起動して 3 秒後に B を別スレッドで起動し、
両者は別々の SqlSession（別々の DB セッション）で同じ社員 10001 を扱います。

- `OptimisticLockMain1 & OptimisticLockMain2`: 両方が同じバージョンを読みます。先に UPDATE した 1 が確定し、
  2 の UPDATE は `Updates: 0` になって「楽観ロックエラー発生」の例外で終わります（これが確認したい動きです）。
- `PessimisticLockMain1 & PessimisticLockMain2`: 1 が `SELECT ... FOR UPDATE` で行を押さえて 15 秒待つ間、
  2 の `SELECT ... FOR UPDATE` は結果を返さずに待たされ、1 のコミット後に進みます。

HSQLDB の既定の LOCKS 方式はテーブル単位のロックを使うため、MySQL の行ロックと待機の範囲が同じになるとは限りません。
自動テスト（Gradle: test）では、古いバージョンでの更新が 0 件になり、給与が上書きされないことを確かめます。
