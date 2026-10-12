# mybatis-dynamic — foreach・任意条件・部署別集約（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/08_mybatis-dynamic/mybatis-dynamic` を、Codinable で単独で動くようにしたものです。

部署番号・給与下限・社員番号のリストを任意に組み合わせる動的 SQL と、部署別の集約結果を DTO で受け取る例です。

## 読む順番

1. `EmployeeFilter` で、どの条件が任意（null 可）かを確認する。
2. `src/main/resources/pro/kensait/db/mybatisdynamic/CompanyMapper.xml` の `<where>`・`<if>`・`<foreach>` と、
   集約の SELECT（`summarizeDepartments`）を読む。
3. `CompanyMapperTest` で、条件の組み合わせごとに返る社員と、部署ごとの集約結果を確認する。

値は SQL 文字列に連結せずバインドします。社員が 0 人の部署があると、INNER JOIN と LEFT JOIN で集約結果が変わります。

## Codinable での動かし方

- テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。
- `CompanyMapperTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:mybatis_dynamic`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「部署別の集約を DTO で受け取る」・「foreach と集合条件」のコードが読み書きするテーブルです。
テーブルはテストの準備で、インメモリの HSQLDB に作られます（`src/test/java/pro/kensait/db/mybatisdynamic/CompanyMapperTest.java`）。

### DEPARTMENT

| カラム | 型 | 制約 |
|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー |
| `DEPARTMENT_NAME` | `VARCHAR(100)` | NOT NULL |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME |
|---|---|
| 1 | PLANNING |
| 2 | HR |
| 3 | SALES |
| 4 | PRODUCT |

### EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `SALARY` | `DECIMAL(12, 2)` | NOT NULL |

初期データ（16 件。先頭 12 件）

| EMPLOYEE_ID | DEPARTMENT_ID | EMPLOYEE_NAME | SALARY |
|---|---|---|---|
| 10001 | 3 | Alice | 500000.00 |
| 10002 | 1 | Bob | 450000.00 |
| 10003 | 2 | Carol | 350000.00 |
| 10004 | 3 | Dave | 400000.00 |
| 10005 | 3 | Ellen | 300000.00 |
| 10006 | 1 | Frank | 250000.00 |
| 10007 | 4 | Ivan | 480000.00 |
| 10008 | 2 | Justin | 460000.00 |
| 10009 | 4 | Mallory | 420000.00 |
| 10010 | 3 | Matilda | 280000.00 |
| 10011 | 4 | Oscar | 320000.00 |
| 10012 | 4 | Pat | 240000.00 |
<!-- schema:end -->
