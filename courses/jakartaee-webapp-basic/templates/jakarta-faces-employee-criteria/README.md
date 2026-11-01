# jakarta-faces-employee-criteria — Criteria API と静的メタモデルによる検索

チャプター 11.1「Criteria APIとJakarta Dataによる検索」のうち、Criteria API の演習です。チャプター 10.1 の `jakarta-faces-employee-jpa` と
同じ画面・Service のまま、`EmployeeRepository` の検索と件数だけを、指定された条件だけを組み立てる Criteria API に書き換えています。
条件の属性は、ビルドのときにアノテーションプロセッサがエンティティから生成する静的メタモデル（`Employee_` など）で参照します。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/employee/` の下にあります。

| ファイル | 内容 |
|---|---|
| `entity/Employee.java`、`Department.java`、`Job.java` | `@Entity`・`@Table`・`@Column` のマッピング、`@ManyToOne` の関連、`@Version` |
| `src/main/resources/META-INF/persistence.xml` | 永続化ユニット `EmployeePU`。JTA データソース `java:app/jdbc/EmployeeDS` |
| `repository/EmployeeRepository.java` | `search`・`count` が同じ `predicates` で条件を作る。値が null の条件は加えない |
| `build.gradle` | `annotationProcessor` に EclipseLink のメタモデル生成を指定している。生成された `Employee_`・`Department_`・`Job_` は、ビルドのたびに `build/generated/sources/annotationProcessor/java/main` へ作り直される（ソースとしては置かない） |
| `service/EmployeeService.java` | `create`（persist → flush で採番）、`update`（version の比較と `OptimisticLockException`）、`delete`（状態の変更だけ） |
| `web/EmployeeTableBean.java`、`web/EmployeeFormBean.java` | 一覧（検索・ページ送り・削除）と、登録・編集画面のバッキングBean |
| `src/main/webapp/EmployeeTablePage.xhtml`、`EmployeeFormPage.xhtml` | 一覧画面と、登録・編集画面（version を `h:inputHidden` で保持） |
| `config/DataSourceConfig.java`、`config/DatabaseInitializer.java`、`sql/hsqldb/` | データソースの定義と、起動時にテーブルを作り直す演習用の処理 |
| `curl/01_search.sh` 〜 `curl/04_delete.sh` | 検索・登録・更新の競合・削除を HTTP の要求で確かめるスクリプト（`curl/faces.inc` はそれらが読み込む補助の関数） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-employee-criteria/EmployeeTablePage.xhtml` が開く
2. 検索・ページ送り・新規登録・編集・削除を操作する
3. `curl/01_search.sh` 〜 `curl/04_delete.sh` を開いて、エディタの右上の ▶ 実行で 1 つずつ流す
   （登録・更新・削除でデータが変わる。▶ アプリ実行でデプロイし直すと初期データに戻る）

## 結果のどこを見るか

- 実行結果（Payara のログ）に、Criteria API から作られた SQL が出る。部署で絞り込んだときだけ `DEPARTMENT` との結合が現れ、
  指定しなかったキーワード・役職・月給の条件は SQL に現れない
- 部署を持たない Victor は、部署で絞り込まないときには一覧に残る（チャプター 10.1 の JPQL では `LEFT JOIN` が必要だった）
- `Employee_.employeeName` のようなメタモデルの属性を使っているので、エンティティの属性名を変えると、古い名前を使った箇所がコンパイルエラーになる
- 一覧と総件数は同じ `predicates` から作るので、表示と件数がずれない

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「Criteria API と静的メタモデルによる検索」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_EMPLOYEE_DDL.sql`、初期データ: `sql/hsqldb/3_EMPLOYEE_DML.sql`）。

### DEPARTMENT

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

### JOB

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `JOB_ID` | `INT` | 主キー | 役職ID |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL | 役職名 |
| `GRADE` | `INT` | NOT NULL | 等級 |

初期データ（4 件）

| JOB_ID | JOB_NAME | GRADE |
|---|---|---|
| 1 | ASSOCIATE | 1 |
| 2 | CHIEF | 2 |
| 3 | LEADER | 3 |
| 4 | MANAGER | 4 |

### EMPLOYEE

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー、自動採番 | 社員ID（登録時に 10017 から採番） |
| `EMPLOYEE_CODE` | `VARCHAR(8)` | NOT NULL | 社員コード（E + 社員ID） |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 氏名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID（部署なしは NULL） |
| `JOB_ID` | `INT` | 外部キー → `JOB.JOB_ID`、NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社日 |
| `STATUS` | `VARCHAR(10)` | NOT NULL、既定値 'active' | 状態（active / deleted。論理削除） |
| `VERSION` | `INT` | NOT NULL、既定値 0 | バージョン（更新競合の検出） |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_CODE | EMPLOYEE_NAME | DEPARTMENT_ID | JOB_ID | SALARY | ENTRANCE_DATE | STATUS | VERSION |
|---|---|---|---|---|---|---|---|---|
| 10001 | E10001 | Alice | 3 | 4 | 500000 | 2012-04-01 | active | 0 |
| 10002 | E10002 | Bob | 1 | 4 | 450000 | 2012-04-01 | active | 0 |
| 10003 | E10003 | Carol | 2 | 2 | 350000 | 2012-04-01 | active | 0 |
| 10004 | E10004 | Dave | 3 | 3 | 400000 | 2012-04-01 | active | 0 |
| 10005 | E10005 | Ellen | 3 | 2 | 300000 | 2013-04-01 | active | 0 |
| 10006 | E10006 | Frank | 1 | 1 | 250000 | 2013-10-01 | active | 0 |
| 10007 | E10007 | Ivan | 4 | 4 | 480000 | 2014-01-01 | active | 0 |
| 10008 | E10008 | Justin | 2 | 4 | 460000 | 2014-04-01 | active | 0 |
| 10009 | E10009 | Mallory | 4 | 3 | 420000 | 2014-07-01 | active | 0 |
| 10010 | E10010 | Matilda | 3 | 1 | 280000 | 2015-08-01 | active | 0 |
| 10011 | E10011 | Oscar | 4 | 2 | 320000 | 2015-11-01 | active | 0 |
| 10012 | E10012 | Pat | 4 | 1 | 240000 | 2016-04-01 | active | 0 |
<!-- schema:end -->
