# jpa-embedded-id-2 — EmbeddedIdと分社・社員の関連

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-embedded-id-2` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_embedded_id_2` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

「EmbeddedId と分社エンティティの関連」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### SUBSIDIARY（分社）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `SUBSIDIARY_ID` | `INT` | 主キー | 分社ID |
| `SUBSIDIARY_NAME` | `VARCHAR(30)` | NOT NULL | 分社名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |

初期データ（4 件）

| SUBSIDIARY_ID | SUBSIDIARY_NAME | LOCATION |
|---|---|---|
| 1 | GLOBAL | TOKYO HQ |
| 2 | FUTURE | TOKYO HQ |
| 3 | TECH | YOKOHAMA BRANCH |
| 4 | PRODUCT | YOKOHAMA BRANCH |

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `SUBSIDIARY_ID` | `INT` | 主キー（複合）、外部キー → `SUBSIDIARY.SUBSIDIARY_ID` | 分社ID |
| `EMPLOYEE_ID` | `INT` | 主キー（複合） | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（16 件。先頭 12 件）

| SUBSIDIARY_ID | EMPLOYEE_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 1 | 10001 | Bob | 450000 |
| 1 | 10002 | Frank | 250000 |
| 1 | 10003 | Steve | 380000 |
| 2 | 10001 | Carol | 350000 |
| 2 | 10002 | Justin | 460000 |
| 2 | 10003 | Victor | 220000 |
| 3 | 10001 | Alice | 500000 |
| 3 | 10002 | Dave | 400000 |
| 3 | 10003 | Ellen | 300000 |
| 3 | 10004 | Matilda | 280000 |
| 3 | 10005 | Peggy | 270000 |
| 4 | 10001 | Ivan | 480000 |
<!-- schema:end -->
