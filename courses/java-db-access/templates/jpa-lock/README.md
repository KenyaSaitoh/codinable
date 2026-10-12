# jpa-lock — 楽観ロックと悲観ロック

講座リポジトリ learn_java_db_aidd の `projects/master/05_jpa-crud/jpa-lock` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_lock` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

## Codinable での動かし方

- ツールバーの **▶ アプリ実行** を押します。`mains.txt` に並べた main を上から順に実行し、
  出力は `===== [番号/件数] クラス名 =====` で区切られます。
- main ごとに `sql/hsqldb/*.sql`（テーブルの削除・作成・初期データ）を流し直すので、どの main も初期データから始まります。
  DB は HSQLDB のインメモリ（`jdbc:hsqldb:mem:testdb`）で、元の教材の HSQLDB サーバーや `setupHsqldb` は使いません。
- マッピングと各 main の実行はテストでも確かめられます。`JpaMappingTest.java`・`SampleMainTest.java`・`OptimisticLockTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。
- main を 1 つだけ動かしたいときは、そのクラスを開いてエディタの右上の **▶ 実行** を押します（このときも先に初期データへ戻します）。
  `mains.txt` のほかの行の先頭に `#` を付けてから **▶ アプリ実行** を押しても同じです。
- 起動クラス `pro.kensait.course.SampleLauncher` と `mains.txt` は Codinable 用に足したものです。教材の main は元のままです。

## 読む順番

1. 下の「この演習で使うテーブル」（テーブル構成・初期データ。元は `sql/hsqldb/2_*.sql` と `3_*.sql`）を確認する。
2. `src/main/resources/META-INF/persistence.xml` とエンティティの注釈を確認する。
3. **▶ アプリ実行** で main を動かし、SQL・取得結果・更新後のデータを照合する。

## 実行する main（mains.txt の順）

- `pro.kensait.jpa.company.optimistic.main.LockMain` と `pro.kensait.jpa.company.optimistic.conflictor.LockConflictor`（同時に実行）
- `pro.kensait.jpa.company.pessimistic.main.ReadLockMain` と `pro.kensait.jpa.company.pessimistic.conflictor.ReadLockConflicter`（同時に実行）
- `pro.kensait.jpa.company.pessimistic.main.WriteLockMain` と `pro.kensait.jpa.company.pessimistic.conflictor.WriteLockConflicter`（同時に実行）
- `pro.kensait.jpa.company.pessimistic.main.WriteLockForceMain` と `pro.kensait.jpa.company.pessimistic.conflictor.WriteLockForceConflicter`（同時に実行）

## ロックの確認

元の教材では、HSQLDB サーバーに 2 つのプロセスから接続し、最初のプログラムが sleep に入った後に競合用のプログラムを起動していました。
Codinable では `mains.txt` の `A & B` の行がこれを 1 回の実行で再現します。A を起動して 3 秒後に B を別スレッドで起動し、
両者は別々の EntityManager（別々の DB セッション）で同じ社員 10003 を扱います。`[  3.0 秒] ... を開始` のような行で、
どちらがいつ始まり、いつ終わったかを追えます。

- `LockMain & LockConflictor`: LockConflictor が先に更新して確定するため、20 秒後の LockMain の flush が
  `OptimisticLockException` になります（例外で終わるのが確認したい動きです）。
- `ReadLockMain & ReadLockConflicter`、`WriteLockMain & WriteLockConflicter`、`WriteLockForceMain & WriteLockForceConflicter`:
  先に起動した側がロックを持ったまま 20 秒待つので、競合側の終了はそのコミットより後になります。

HSQLDB の既定の LOCKS 方式はテーブル単位のロックを使うため、MySQL の行ロックと待機の範囲が同じになるとは限りません。
自動テスト（`OptimisticLockTest.java` を開いてエディタの右上の ▶ 実行）では、sleep のタイミングに頼らずに、古いバージョンでの更新が `OptimisticLockException` になることを確かめます。
4 組の実行には合わせて 1 分半ほどかかります。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「JPA の @Version と悲観ロック」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` |  | 部署名 |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `VERSION` | `INT` |  | バージョン |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_NAME | SALARY | VERSION |
|---|---|---|---|---|
| 10001 | Alice | SALES | 500000 | 0 |
| 10002 | Bob | PLANNING | 450000 | 0 |
| 10003 | Carol | HR | 350000 | 0 |
| 10004 | Dave | SALES | 400000 | 0 |
| 10005 | Ellen | SALES | 300000 | 0 |
| 10006 | Frank | PLANNING | 250000 | 0 |
| 10007 | Ivan | PRODUCT | 480000 | 0 |
| 10008 | Justin | HR | 460000 | 0 |
| 10009 | Mallory | PRODUCT | 420000 | 0 |
| 10010 | Matilda | SALES | 280000 | 0 |
| 10011 | Oscar | PRODUCT | 320000 | 0 |
| 10012 | Pat | PRODUCT | 240000 | 0 |
<!-- schema:end -->
