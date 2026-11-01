# jpa-crud — EntityManager と Repository（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/05_jpa-crud/jpa-crud` を、Codinable で単独で動くようにしたものです。

`EntityManagerFactory` は共有する生成器、`EntityManager` は処理単位の永続化コンテキストです。
managed なエンティティの変更は flush 時に SQL へ反映されます。

## 読む順番

1. `src/main/resources/META-INF/persistence.xml` で永続化ユニットと接続先を確認する。
2. `Employee`（エンティティ）と `EmployeeRepository`（EntityManager を引数で受け取る）を読む。
3. `EmployeeRepositoryTest` で、トランザクションを呼び出し元が開始・確定する分担を確認する。

`@Version` の値が増えるだけでなく、古いバージョンでの更新が拒否されることは `jpa-lock` のテストで確認します。

## Codinable での動かし方

- テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。
- `EmployeeRepositoryTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:jpa_crud`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。

<!-- schema:begin -->
## この演習で使うテーブル

「Repository とトランザクションの分担」のコードが読み書きするテーブルです。

この演習には DDL がありません。`META-INF/persistence.xml` の
`schema-generation.database.action = drop-and-create` により、Hibernate がエンティティ
（`Employee.java`）の定義からテーブルを作ります（インメモリの HSQLDB）。
型は Hibernate が Java の型と `@Column` の指定から決めます。

<!-- この説明は手で書いている (desktop/scripts/build-schema-docs.js の MANUAL)。
     エンティティを変えたら、ここも合わせて直すこと -->

### EMPLOYEE（社員）

| カラム | Java のフィールド | 型の元になる指定 | 制約 |
|---|---|---|---|
| `EMPLOYEE_ID` | `Integer id` | `@Id` | 主キー |
| `DEPARTMENT_ID` | `Integer departmentId` | `nullable = false` | NOT NULL |
| `EMPLOYEE_NAME` | `String name` | `length = 100` → `VARCHAR(100)` | NOT NULL |
| `SALARY` | `BigDecimal salary` | `precision = 12, scale = 2` → `DECIMAL(12, 2)` | NOT NULL |
| `VERSION` | `long version` | `@Version` | 楽観ロックの版番号（更新のたびに Hibernate が 1 増やす） |

初期データはありません。テスト（`EmployeeRepositoryTest.java`）が社員 10017「Walter」（部署 3、月給 230000）を登録し、
月給を 250000 に更新してから削除します。
<!-- schema:end -->
