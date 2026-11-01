# jpa-relation-1 — 1対多／多対1・fetch・cascade

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-relation-1` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_relation_1` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

## Codinable での動かし方

- ツールバーの **▶ アプリ実行** を押します。`mains.txt` に並べた main を上から順に実行し、
  出力は `===== [番号/件数] クラス名 =====` で区切られます。
- main ごとに `sql/hsqldb/*.sql`（テーブルの削除・作成・初期データ）を流し直すので、どの main も初期データから始まります。
  DB は HSQLDB のインメモリ（`jdbc:hsqldb:mem:testdb`）で、元の教材の HSQLDB サーバーや `setupHsqldb` は使いません。
- マッピングと各 main の実行はテストでも確かめられます。`JpaMappingTest.java`・`SampleMainTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。
- main を 1 つだけ動かしたいときは、そのクラスを開いてエディタの右上の **▶ 実行** を押します（このときも先に初期データへ戻します）。
  `mains.txt` のほかの行の先頭に `#` を付けてから **▶ アプリ実行** を押しても同じです。
- 起動クラス `pro.kensait.course.SampleLauncher` と `mains.txt` は Codinable 用に足したものです。教材の main は元のままです。

## 読む順番

1. 下の「この演習で使うテーブル」（テーブル構成・初期データ。元は `sql/hsqldb/2_*.sql` と `3_*.sql`）を確認する。
2. `src/main/resources/META-INF/persistence.xml` とエンティティの注釈を確認する。
3. **▶ アプリ実行** で main を動かし、SQL・取得結果・更新後のデータを照合する。

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

元の教材では 2 つのプロセスを同時に動かしていた例です。Codinable では `mains.txt` の最後の行
`CascadeOptimisticLockMain1 & CascadeOptimisticLockMain2` が、1 を起動して 3 秒後に 2 を別スレッドで起動します。
1 は部署とその社員を読み、社員の給与を変えて部署を merge した後 20 秒待ちます。その間に 2 が同じ社員を更新するため、
cascade で社員へ伝わった 1 の更新は古いバージョンとなり、`OptimisticLockException` で終わります
（親を merge しても、カスケードされた子の楽観ロックが効くことを確かめる例です）。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「1対多・多対1、fetch と cascade」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |
| `VERSION` | `INT` |  | バージョン |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION | VERSION |
|---|---|---|---|
| 1 | PLANNING | TOKYO HQ | 0 |
| 2 | HR | TOKYO HQ | 0 |
| 3 | SALES | YOKOHAMA BRANCH | 0 |
| 4 | PRODUCT | YOKOHAMA BRANCH | 0 |

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` |  | 部署ID |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社年月日 |
| `JOB_ID` | `INT` | NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `VERSION` | `INT` |  | バージョン |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | ENTRANCE_DATE | JOB_ID | SALARY | VERSION |
|---|---|---|---|---|---|---|
| 10001 | Alice | 3 | 2012-04-01 | 0 | 500000 | 0 |
| 10002 | Bob | 1 | 2012-04-01 | 0 | 450000 | 0 |
| 10003 | Carol | 2 | 2012-04-01 | 2 | 350000 | 0 |
| 10004 | Dave | 3 | 2012-04-01 | 1 | 400000 | 0 |
| 10005 | Ellen | 3 | 2013-04-01 | 2 | 300000 | 0 |
| 10006 | Frank | 1 | 2013-10-01 | 3 | 250000 | 0 |
| 10007 | Ivan | 4 | 2014-01-01 | 0 | 480000 | 0 |
| 10008 | Justin | 2 | 2014-04-01 | 0 | 460000 | 0 |
| 10009 | Mallory | 4 | 2014-07-01 | 1 | 420000 | 0 |
| 10010 | Matilda | 3 | 2015-08-01 | 3 | 280000 | 0 |
| 10011 | Oscar | 4 | 2015-11-01 | 2 | 320000 | 0 |
| 10012 | Pat | 4 | 2016-04-01 | 3 | 240000 | 0 |
<!-- schema:end -->
