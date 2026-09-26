# jpa-lock — 楽観ロックと悲観ロック

講座リポジトリ learn_java_db_aidd の `projects/master/05_jpa-crud/jpa-lock` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_lock` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.jpa.company.optimistic.main.LockMain` と `pro.kensait.jpa.company.optimistic.conflictor.LockConflictor`（同時に実行）
- `pro.kensait.jpa.company.pessimistic.main.ReadLockMain` と `pro.kensait.jpa.company.pessimistic.conflictor.ReadLockConflicter`（同時に実行）
- `pro.kensait.jpa.company.pessimistic.main.WriteLockMain` と `pro.kensait.jpa.company.pessimistic.conflictor.WriteLockConflicter`（同時に実行）
- `pro.kensait.jpa.company.pessimistic.main.WriteLockForceMain` と `pro.kensait.jpa.company.pessimistic.conflictor.WriteLockForceConflicter`（同時に実行）

## ロックの確認

元の教材では、HSQLDB サーバーに 2 つのターミナルから接続し、最初のプログラムが sleep に入った後に競合用のプログラムを起動していました。
Codinable では `mains.txt` の `A & B` の行がこれを 1 回の実行で再現します。A を起動して 3 秒後に B を別スレッドで起動し、
両者は別々の EntityManager（別々の DB セッション）で同じ社員 10003 を扱います。`[  3.0 秒] ... を開始` のような行で、
どちらがいつ始まり、いつ終わったかを追えます。

- `LockMain & LockConflictor`: LockConflictor が先に更新して確定するため、20 秒後の LockMain の flush が
  `OptimisticLockException` になります（例外で終わるのが確認したい動きです）。
- `ReadLockMain & ReadLockConflicter`、`WriteLockMain & WriteLockConflicter`、`WriteLockForceMain & WriteLockForceConflicter`:
  先に起動した側がロックを持ったまま 20 秒待つので、競合側の終了はそのコミットより後になります。

HSQLDB の既定の LOCKS 方式はテーブル単位のロックを使うため、MySQL の行ロックと待機の範囲が同じになるとは限りません。
自動テスト（Gradle: test）では、sleep のタイミングに頼らずに、古いバージョンでの更新が `OptimisticLockException` になることを確かめます。
4 組の実行には合わせて 1 分半ほどかかります。
