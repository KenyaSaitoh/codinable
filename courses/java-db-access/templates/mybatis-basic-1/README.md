# mybatis-basic-1 — SqlSession・Mapperインターフェース・CRUD

講座リポジトリ learn_java_db_aidd の `projects/master/07_mybatis-crud/mybatis-basic-1` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/mybatis_basic_1` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

## Codinable での動かし方

- ツールバーの **▶ アプリ実行** を押します。`mains.txt` に並べた main を上から順に実行し、
  出力は `===== [番号/件数] クラス名 =====` で区切られます。
- main ごとに `sql/hsqldb/*.sql`（テーブルの削除・作成・初期データ）を流し直すので、どの main も初期データから始まります。
  DB は HSQLDB のインメモリ（`jdbc:hsqldb:mem:testdb`）で、元の教材の HSQLDB サーバーや `setupHsqldb` は使いません。
- マッピングと各 main の実行はテストでも確かめられます。`MyBatisMappingTest.java`・`SampleMainTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。
- main を 1 つだけ動かしたいときは、そのクラスを開いてエディタの右上の **▶ 実行** を押します（このときも先に初期データへ戻します）。
  `mains.txt` のほかの行の先頭に `#` を付けてから **▶ アプリ実行** を押しても同じです。
- 起動クラス `pro.kensait.course.SampleLauncher` と `mains.txt` は Codinable 用に足したものです。教材の main は元のままです。

## 読む順番

1. 下の「この演習で使うテーブル」（テーブル構成・初期データ。元は `sql/hsqldb/2_*.sql` と `3_*.sql`）を確認する。
2. `src/main/resources/mybatis-config.xml` と `mybatis-mapper*.xml`を確認する。
3. **▶ アプリ実行** で main を動かし、SQL・取得結果・更新後のデータを照合する。

## 実行する main（mains.txt の順）

- `pro.kensait.mybatis.company.main1.SelectMain1`
- `pro.kensait.mybatis.company.main1.SelectMain2`
- `pro.kensait.mybatis.company.main1.SelectMain3`
- `pro.kensait.mybatis.company.main1.SelectMain4`
- `pro.kensait.mybatis.company.main1.SelectMain5`
- `pro.kensait.mybatis.company.main1.SelectMain6`
- `pro.kensait.mybatis.company.main1.SelectMain7`
- `pro.kensait.mybatis.company.main1.InsertMain1`
- `pro.kensait.mybatis.company.main1.UpdateMain1`
- `pro.kensait.mybatis.company.main1.UpdateMain2`
- `pro.kensait.mybatis.company.main1.UpdateMain3`
- `pro.kensait.mybatis.company.main1.DeleteMain1`
- `pro.kensait.mybatis.company.main1.DeleteMain2`
- `pro.kensait.mybatis.company.main2.MapperSelectMain1`
- `pro.kensait.mybatis.company.main2.MapperSelectMain2`
- `pro.kensait.mybatis.company.main2.MapperInsertMain1`
- `pro.kensait.mybatis.company.main2.MapperUpdateMain1`
- `pro.kensait.mybatis.company.main2.MapperUpdateMain2`
- `pro.kensait.mybatis.company.main2.MapperDeleteMain1`

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「SqlSession と Mapper による CRUD」のコードが読み書きするテーブルです。
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
<!-- schema:end -->
