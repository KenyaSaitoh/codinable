# jpa-inheritance-2 — JOINEDによるエンティティ継承

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-inheritance-2` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_inheritance_2` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

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

「JOINED による継承」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

### DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION |
|---|---|---|
| 1 | PLANNING | TOKYO HQ |
| 2 | HR | TOKYO HQ |
| 3 | SALES | YOKOHAMA BRANCH |
| 4 | PRODUCT | YOKOHAMA BRANCH |

### EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID |
| `EMPLOYEE_TYPE` | `CHAR(1)` | NOT NULL | 社員種別 |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社年月日 |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | EMPLOYEE_TYPE | ENTRANCE_DATE |
|---|---|---|---|---|
| 10001 | Alice | 3 | 1 | 2012-04-01 |
| 10002 | Bob | 1 | 1 | 2012-04-01 |
| 10003 | Carol | 2 | 1 | 2012-04-01 |
| 10004 | Dave | 3 | 1 | 2012-04-01 |
| 10005 | Ellen | 3 | 1 | 2013-04-01 |
| 10006 | Frank | 1 | 1 | 2013-10-01 |
| 10007 | Ivan | 4 | 1 | 2014-01-01 |
| 10008 | Justin | 2 | 1 | 2014-04-01 |
| 10009 | Mallory | 4 | 1 | 2014-07-01 |
| 10010 | Matilda | 3 | 1 | 2015-08-01 |
| 10011 | Oscar | 4 | 1 | 2015-11-01 |
| 10012 | Pat | 4 | 2 | 2016-04-01 |

### FULLTIMER（正社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL | 役職名 |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（13 件。先頭 12 件）

| EMPLOYEE_ID | JOB_NAME | SALARY |
|---|---|---|
| 10001 | MANAGER | 500000 |
| 10002 | MANAGER | 450000 |
| 10003 | CHIEF | 350000 |
| 10004 | LEADER | 400000 |
| 10005 | CHIEF | 300000 |
| 10006 | ASSOCIATE | 250000 |
| 10007 | MANAGER | 480000 |
| 10008 | MANAGER | 460000 |
| 10009 | LEADER | 420000 |
| 10010 | ASSOCIATE | 280000 |
| 10011 | CHIEF | 320000 |
| 10015 | LEADER | 380000 |

### PARTTIMER（パートタイマー）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `PARTTIMER_PAYMENT` | `INT` | NOT NULL | 時給 |

初期データ（3 件）

| EMPLOYEE_ID | PARTTIMER_PAYMENT |
|---|---|
| 10012 | 2000 |
| 10013 | 2400 |
| 10014 | 1700 |
<!-- schema:end -->
