# jakarta-data-employee — Jakarta Data の Repository とページング

チャプター 11.1「Criteria APIとJakarta Dataによる検索」のうち、Jakarta Data 1.0 の演習です。エンティティはチャプター 10.1 と同じ
`Employee`・`Department`・`Job` で、データアクセス層の `EmployeeRepository` をインタフェースの宣言だけで書きます。
実装クラスは Jakarta Data のプロバイダー（Payara Server が持つ）が生成し、CDI Bean として `EmployeeService` に注入されます。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/employee/` の下にあります。

| ファイル | 内容 |
|---|---|
| `repository/EmployeeRepository.java` | `@Repository` の `CrudRepository`。メソッド名による問い合わせ、`@Find`（`byCode`・`byStatus`）、`@Query`（JDQL の `searchByNameAndSalary`） |
| `repository/DepartmentRepository.java`、`JobRepository.java` | 読み取りだけの `BasicRepository` |
| `service/EmployeeService.java` | `PageRequest.ofPage` と `Page` によるページング、`insert`・`update` の明示的な呼び出し、`OptimisticLockingFailureException` の変換 |
| `web/EmployeeTableBean.java`、`src/main/webapp/EmployeeTablePage.xhtml` | 氏名と月給の範囲で検索する一覧。社員コードで編集画面を開く欄と、在籍中の人数（`countByStatus`） |
| `web/EmployeeFormBean.java`、`src/main/webapp/EmployeeFormPage.xhtml` | 登録・編集画面（チャプター 10.1 と同じ） |
| `entity/`、`src/main/resources/META-INF/persistence.xml` | チャプター 10.1 と同じエンティティと永続化ユニット `EmployeePU` |
| `config/DataSourceConfig.java`、`config/DatabaseInitializer.java`、`sql/hsqldb/` | データソースの定義と、起動時にテーブルを作り直す演習用の処理 |
| `curl/01_search.sh` 〜 `curl/04_delete.sh` | 検索・登録・更新の競合・削除を HTTP の要求で確かめるスクリプト（`curl/faces.inc` はそれらが読み込む補助の関数） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-data-employee/EmployeeTablePage.xhtml` が開く
2. 氏名と月給の範囲で検索し、「次へ」でページを送る。社員コード（`E10002` など）で編集画面を開く。新規登録・編集・削除も操作する
3. `curl/01_search.sh` 〜 `curl/04_delete.sh` を開いて、エディタの右上の ▶ 実行で 1 つずつ流す
   （登録・更新・削除でデータが変わる。▶ アプリ実行でデプロイし直すと初期データに戻る）

## 結果のどこを見るか

- `EmployeeRepository` には実装クラスが無いが、`EmployeeService` から呼べる。デプロイの時点で、プロバイダーが問い合わせを検証して実装を用意している
- 検索の条件は `@Query` に固定されている（氏名の部分一致と月給の範囲）。空欄は「すべて」として `""`・`0`・`9999999` を渡している。
  部署や役職のように、入力によって条件そのものが変わる検索は Criteria API（`jakarta-faces-employee-criteria`）で組み立てる
- 1 ページ目は `PageRequest.ofPage(1, 5, true)`。Jakarta Data のページ番号は 1 から始まり、`true` で総件数も問い合わせる
- 更新と論理削除は `employeeRepository.update(...)` を呼んで保存する。Repository は状態を持たない前提なので、setter を呼べば保存される、という振る舞いには頼らない
- `03_conflict.sh`: 同じ社員を 2 人が編集すると、後から保存した側は「ほかの人が先に更新しました」になる

## 仕様と Payara Server 7.2026.9 の違い

この演習には、Payara Server 7.2026.9 の Jakarta Data の実装に合わせた箇所が 2 つあります。

- `EmployeeService#create` の `entityManager.flush()`: Jakarta Data 1.0 の仕様では、`insert` の戻り値に採番された ID が入る
  （`jakarta.data.repository.Insert` の API ドキュメント）。Payara 7.2026.9 の `insert` は INSERT を送る前に戻り、
  社員 ID が `null` のままになる（社員コードが `Enull` になる）ので、同じトランザクションの永続化コンテキストを flush して ID を確定させている。
  講座のコードには無い、この演習だけの回避策
- `searchByNameAndSalary` の `@Query` に `from Employee` と `order by employeeId` を書いている: 仕様上はどちらも省略でき、
  並び順は `@OrderBy` でも指定できる（Jakarta Data 1.0 の 5.5.1・4.7.1）。Payara 7.2026.9 では、`Page` を返すメソッドで
  `from` の無い問い合わせが失敗し、`@Query` に付けた `@OrderBy` も効かないため、仕様の範囲内で両方を問い合わせに書いている（講座のコードも同じ）

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「Jakarta Data の Repository とページング」のコードが読み書きするテーブルです。
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
