# jpa-embedded-id-1 — EmbeddedIdによる複合主キー

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-embedded-id-1` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_embedded_id_1` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

## Codinable での動かし方

- ツールバーの **▶ アプリ実行** を押します。`mains.txt` に並べた main を上から順に実行し、
  出力は `===== [番号/件数] クラス名 =====` で区切られます。
- main ごとに `sql/hsqldb/*.sql`（テーブルの削除・作成・初期データ）を流し直すので、どの main も初期データから始まります。
  DB は HSQLDB のインメモリ（`jdbc:hsqldb:mem:testdb`）で、元の教材の HSQLDB サーバーや `setupHsqldb` は使いません。
- マッピングと各 main の実行はテストでも確かめられます。`JpaMappingTest.java`・`SampleMainTest.java`・`PrimaryKeyTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。
- main を 1 つだけ動かしたいときは、そのクラスを開いてエディタの右上の **▶ 実行** を押します（このときも先に初期データへ戻します）。
  `mains.txt` のほかの行の先頭に `#` を付けてから **▶ アプリ実行** を押しても同じです。
- 起動クラス `pro.kensait.course.SampleLauncher` と `mains.txt` は Codinable 用に足したものです。教材の main は元のままです。

## 読む順番

1. 下の「この演習で使うテーブル」（テーブル構成・初期データ。元は `sql/hsqldb/2_*.sql` と `3_*.sql`）を確認する。
2. `src/main/resources/META-INF/persistence.xml` とエンティティの注釈を確認する。
3. **▶ アプリ実行** で main を動かし、SQL・取得結果・更新後のデータを照合する。

## 実行する main（mains.txt の順）

- `pro.kensait.jpa.company.main.EmbeddedIdSelectMain`
- `pro.kensait.jpa.company.main.EmbeddedIdInsertMain`
- `pro.kensait.jpa.company.main.EmbeddedIdUpdateMain`
- `pro.kensait.jpa.company.main.EmbeddedIdDeleteMain`

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「@EmbeddedId による複合主キー」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `SUBSIDIARY_NAME` | `VARCHAR(30)` | 主キー（複合） | 分社名 |
| `EMPLOYEE_ID` | `INT` | 主キー（複合） | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（16 件。先頭 12 件）

| SUBSIDIARY_NAME | EMPLOYEE_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| FUTURE | 10001 | Carol | 350000 |
| FUTURE | 10002 | Justin | 460000 |
| FUTURE | 10003 | Victor | 220000 |
| GLOBAL | 10001 | Bob | 450000 |
| GLOBAL | 10002 | Frank | 250000 |
| GLOBAL | 10003 | Steve | 380000 |
| PRODUCT | 10001 | Ivan | 480000 |
| PRODUCT | 10002 | Mallory | 420000 |
| PRODUCT | 10003 | Oscar | 320000 |
| PRODUCT | 10004 | Pat | 240000 |
| PRODUCT | 10005 | Trent | 310000 |
| TECH | 10001 | Alice | 500000 |
<!-- schema:end -->
