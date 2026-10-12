# jpa-embeddable — Embeddable・Embeddedによる値オブジェクト

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-embeddable` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_embeddable` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.jpa.company.main.EmbeddableSelectMain`

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「@Embeddable による値オブジェクト」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | ビル名 |
| `POSTAL_CODE` | `VARCHAR(10)` |  | 郵便番号 |
| `TOWN` | `VARCHAR(10)` |  | 市町村 |
| `STREET` | `VARCHAR(20)` |  | 番地 |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION | POSTAL_CODE | TOWN | STREET |
|---|---|---|---|---|---|
| 1 | PLANNING | TOKYO HQ | 100-0000 | 東京都千代田区 | 1-2-3 |
| 2 | HR | TOKYO HQ | 100-0000 | 東京都千代田区 | 1-2-3 |
| 3 | SALES | YOKOHAMA BRANCH | 160-0001 | 東京都新宿区 | 1-2-3 |
| 4 | PRODUCT | YOKOHAMA BRANCH | 160-0001 | 東京都新宿区 | 1-2-3 |

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID |
| `JOB_ID` | `INT` | NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `ZIP_CODE` | `VARCHAR(10)` |  | 郵便番号 |
| `CITY` | `VARCHAR(10)` |  | 市町村 |
| `STREET` | `VARCHAR(20)` |  | 番地 |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | JOB_ID | SALARY | ZIP_CODE | CITY | STREET |
|---|---|---|---|---|---|---|---|
| 10001 | Alice | 3 | 0 | 500000 | 132-0000 | 東京都江戸川区 | 1-2-3 |
| 10002 | Bob | 1 | 0 | 450000 | 279-0000 | 千葉県浦安市 | 1-2-3 |
| 10003 | Carol | 2 | 2 | 350000 | 270-1300 | 千葉県印西市 | 1-2-3 |
| 10004 | Dave | 3 | 1 | 400000 | 221-0000 | 神奈川県横浜市 | 1-2-3 |
| 10005 | Ellen | 3 | 2 | 300000 | 184-0000 | 東京都小金井市 | 1-2-3 |
| 10006 | Frank | 1 | 3 | 250000 | *(NULL)* | *(NULL)* | *(NULL)* |
| 10007 | Ivan | 4 | 0 | 480000 | 182-0000 | 東京都調布市 | 1-2-3 |
| 10008 | Justin | 2 | 0 | 460000 | 220-0000 | 神奈川県横浜市 | 1-2-3 |
| 10009 | Mallory | 4 | 1 | 420000 | 273-0000 | 千葉県船橋市 | 1-2-3 |
| 10010 | Matilda | 3 | 3 | 280000 | *(NULL)* | *(NULL)* | *(NULL)* |
| 10011 | Oscar | 4 | 2 | 320000 | 340-0000 | 埼玉県草加市 | 1-2-3 |
| 10012 | Pat | 4 | 3 | 240000 | 210-0000 | 神奈川県川崎市 | 1-2-3 |
<!-- schema:end -->
