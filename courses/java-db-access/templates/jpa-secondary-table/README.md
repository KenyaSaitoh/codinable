# jpa-secondary-table — SecondaryTableによる複数テーブルへのマッピング

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-secondary-table` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_secondary_table` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

- `pro.kensait.jpa.company.main.SelectMain1`
- `pro.kensait.jpa.company.main.SelectMain2`

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「@SecondaryTable で 2 つの表へ対応させる」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` |  | 部署名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_NAME | SALARY |
|---|---|---|---|
| 10001 | Alice | SALES | 500000 |
| 10002 | Bob | PLANNING | 450000 |
| 10003 | Carol | HR | 350000 |
| 10004 | Dave | SALES | 400000 |
| 10005 | Ellen | SALES | 300000 |
| 10006 | Frank | PLANNING | 250000 |
| 10007 | Ivan | PRODUCT | 480000 |
| 10008 | Justin | HR | 460000 |
| 10009 | Mallory | PRODUCT | 420000 |
| 10010 | Matilda | SALES | 280000 |
| 10011 | Oscar | PRODUCT | 320000 |
| 10012 | Pat | PRODUCT | 240000 |

### ADDRESS（住所）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `ADDRESS_ID` | `INT` | 主キー | 住所ID（社員IDと同じ） |
| `ZIP_CODE` | `VARCHAR(10)` |  | 郵便番号 |
| `PREFECTURE` | `VARCHAR(10)` |  | 都道府県 |
| `CITY` | `VARCHAR(20)` |  | 市町村 |

初期データ（14 件。先頭 12 件）

| ADDRESS_ID | ZIP_CODE | PREFECTURE | CITY |
|---|---|---|---|
| 10001 | 132-0000 | 東京都 | 江戸川区 |
| 10002 | 279-0000 | 千葉県 | 浦安市 |
| 10003 | 270-1300 | 千葉県 | 印西市 |
| 10004 | 221-0000 | 神奈川県 | 横浜市 |
| 10005 | 184-0000 | 東京都 | 小金井市 |
| 10007 | 182-0000 | 東京都 | 調布市 |
| 10008 | 220-0000 | 神奈川県 | 横浜市 |
| 10009 | 273-0000 | 千葉県 | 船橋市 |
| 10011 | 340-0000 | 埼玉県 | 草加市 |
| 10012 | 210-0000 | 神奈川県 | 川崎市 |
| 10013 | 331-0000 | 埼玉県 | さいたま市 |
| 10014 | 120-0000 | 東京都 | 足立区 |
<!-- schema:end -->
