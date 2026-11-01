# jpa-basic-2 — LocalDate・列挙型・LOBのマッピング

講座リポジトリ learn_java_db_aidd の `projects/master/05_jpa-crud/jpa-basic-2` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_basic_2` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.jpa.company.main.SelectMain`
- `pro.kensait.jpa.company.main.InsertMain`
- `pro.kensait.jpa.company.main.UpdateMain`
- `pro.kensait.jpa.company.main.DeleteMain`

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「LocalDate・列挙型・LOB のマッピング」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` |  | 部署名 |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社年月日 |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL | 役職名 |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `PHOTO` | `BLOB` |  | 写真 |
| `VERSION` | `INT` |  | バージョン |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_NAME | ENTRANCE_DATE | JOB_NAME | SALARY | PHOTO | VERSION |
|---|---|---|---|---|---|---|---|
| 10001 | Alice | SALES | 2012-04-01 | MANAGER | 500000 | *(NULL)* | 0 |
| 10002 | Bob | PLANNING | 2012-04-01 | MANAGER | 450000 | *(NULL)* | 0 |
| 10003 | Carol | HR | 2012-04-01 | CHIEF | 350000 | *(NULL)* | 0 |
| 10004 | Dave | SALES | 2012-04-01 | LEADER | 400000 | *(NULL)* | 0 |
| 10005 | Ellen | SALES | 2013-04-01 | CHIEF | 300000 | *(NULL)* | 0 |
| 10006 | Frank | PLANNING | 2013-10-01 | ASSOCIATE | 250000 | *(NULL)* | 0 |
| 10007 | Ivan | PRODUCT | 2014-01-01 | MANAGER | 480000 | *(NULL)* | 0 |
| 10008 | Justin | HR | 2014-04-01 | MANAGER | 460000 | *(NULL)* | 0 |
| 10009 | Mallory | PRODUCT | 2014-07-01 | LEADER | 420000 | *(NULL)* | 0 |
| 10010 | Matilda | SALES | 2015-08-01 | ASSOCIATE | 280000 | *(NULL)* | 0 |
| 10011 | Oscar | PRODUCT | 2015-11-01 | CHIEF | 320000 | *(NULL)* | 0 |
| 10012 | Pat | PRODUCT | 2016-04-01 | ASSOCIATE | 240000 | *(NULL)* | 0 |
<!-- schema:end -->
