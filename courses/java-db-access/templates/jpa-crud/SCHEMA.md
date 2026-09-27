# この演習で使うテーブル

「Repository とトランザクションの分担」のコードが読み書きするテーブルです。

この演習には DDL がありません。`META-INF/persistence.xml` の
`schema-generation.database.action = drop-and-create` により、Hibernate がエンティティ
（`Employee.java`）の定義からテーブルを作ります（インメモリの HSQLDB）。
型は Hibernate が Java の型と `@Column` の指定から決めます。

<!-- この説明は手で書いている (desktop/scripts/build-schema-docs.js の MANUAL)。
     エンティティを変えたら、ここも合わせて直すこと -->

## EMPLOYEE（社員）

| カラム | Java のフィールド | 型の元になる指定 | 制約 |
|---|---|---|---|
| `EMPLOYEE_ID` | `Integer id` | `@Id` | 主キー |
| `DEPARTMENT_ID` | `Integer departmentId` | `nullable = false` | NOT NULL |
| `EMPLOYEE_NAME` | `String name` | `length = 100` → `VARCHAR(100)` | NOT NULL |
| `SALARY` | `BigDecimal salary` | `precision = 12, scale = 2` → `DECIMAL(12, 2)` | NOT NULL |
| `VERSION` | `long version` | `@Version` | 楽観ロックの版番号（更新のたびに Hibernate が 1 増やす） |

初期データはありません。テスト（`EmployeeRepositoryTest.java`）が社員 10017「Walter」（部署 3、月給 230000）を登録し、
月給を 250000 に更新してから削除します。
