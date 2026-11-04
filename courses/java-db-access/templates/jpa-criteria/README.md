# jpa-criteria — Criteria APIと任意検索条件

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-criteria` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_criteria` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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
3. `CriteriaMain1`（固定の条件）→ `CriteriaMain2`（任意の条件を文字列の属性名で組み立てる）→
   `CriteriaMain3`（同じ検索を静的メタモデル `Employee_` で書く）の順に読む。
4. **▶ アプリ実行** で main を動かし、SQL・取得結果を照合する。

## 主なファイル

- `src/main/java/pro/kensait/jpa/company/entity/Employee.java`・`Department.java` — エンティティ
- `src/main/java/pro/kensait/jpa/company/main/CriteriaMain1.java` — 部署 ID と月給の固定条件を Criteria API で組み立てる
- `src/main/java/pro/kensait/jpa/company/main/CriteriaMain2.java` — 部署 ID・月給の下限・上限を任意条件にする。属性は `employee.get("salary")` のように文字列で指定する
- `src/main/java/pro/kensait/jpa/company/main/CriteriaMain3.java` — 部署 ID・月給の下限・氏名の部分一致を任意条件にする。属性は静的メタモデル
  `Employee_.salary`・`Employee_.employeeName`・`Employee_.department`（と `Department_.departmentId`）で指定する
- `build/generated/sources/annotationProcessor/java/main/.../Employee_.java` — 注釈プロセッサ `hibernate-processor` がコンパイル時に生成する静的メタモデル
  （`build.gradle` の `annotationProcessor` 行で有効にしている。手で書くファイルではない）

## 静的メタモデルと LIKE のエスケープ（CriteriaMain3）

`CriteriaMain2` のように属性名を文字列で書くと、綴りの誤りや型の不一致は実行するまで分かりません。
静的メタモデル（`Employee_`）を使うと `employee.get(Employee_.salary)` のように属性を型付きで参照でき、誤りがコンパイルエラーになります。
`Employee_` はエンティティから注釈プロセッサが生成するので、エンティティを変えればメタモデルも追従します。

`CriteriaMain3` の氏名条件は `cb.like(path, "%" + 入力 + "%", '\\')` のように第 3 引数でエスケープ文字を指定しています。
LIKE では `%`（任意の文字列）と `_`（任意の 1 文字）がワイルドカードなので、利用者の入力に含まれる `%` や `_` をそのまま渡すと、
「`%` を含む氏名」を探したつもりが全件に一致する、といったことが起こります。
入力側の `%`・`_`・`\` を `\` で前置きしてからパターンに組み込み、その `\` をエスケープ文字として宣言するのが定石です
（TEST4 は氏名に `%` を含む社員の検索で、エスケープしているので 0 件になります）。

## 実行する main（mains.txt の順）

- `pro.kensait.jpa.company.main.CriteriaMain1`
- `pro.kensait.jpa.company.main.CriteriaMain2`
- `pro.kensait.jpa.company.main.CriteriaMain3`

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「Criteria API による任意条件の検索」のコードが読み書きするテーブルです。
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
