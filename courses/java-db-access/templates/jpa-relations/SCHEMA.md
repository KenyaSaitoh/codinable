# この演習で使うテーブル

「双方向の整合と fetch join」「CompanyQuery の Criteria による給与条件」のコードが読み書きするテーブルです。

この演習には DDL がありません。`META-INF/persistence.xml` の
`schema-generation.database.action = drop-and-create` により、Hibernate がエンティティ
（`Department.java`・`Employee.java`）の定義からテーブルを作ります（インメモリの HSQLDB）。
カラム名を指定していないフィールドは、フィールド名がそのままカラム名になります。

<!-- この説明は手で書いている (desktop/scripts/build-schema-docs.js の MANUAL)。
     エンティティを変えたら、ここも合わせて直すこと -->

```
DEPARTMENT（部署・親）             EMPLOYEE（社員・子）
  ID  ◀──────────────────────────  DEPARTMENT_ID（外部キー）
  @OneToMany(mappedBy="department")  @ManyToOne(fetch = LAZY)
```

## DEPARTMENT（部署）

| カラム | Java のフィールド | 制約 |
|---|---|---|
| `ID` | `Integer id` | 主キー |
| `NAME` | `String name` | |

`employees`（`@OneToMany(mappedBy = "department")`）はカラムを持ちません。
関連の持ち主は EMPLOYEE 側の `DEPARTMENT_ID` です。

## EMPLOYEE（社員）

| カラム | Java のフィールド | 制約 |
|---|---|---|
| `ID` | `Integer id` | 主キー |
| `NAME` | `String name` | |
| `SALARY` | `BigDecimal salary` | |
| `DEPARTMENT_ID` | `Department department` | 外部キー → `DEPARTMENT.ID`、NOT NULL（`optional = false`） |

## テストが登録するデータ（`CompanyQueryTest.java`）

| DEPARTMENT.ID | DEPARTMENT.NAME |
|---|---|
| 3 | SALES |

| EMPLOYEE.ID | NAME | SALARY | DEPARTMENT_ID |
|---|---|---|---|
| 10001 | Alice | 500000 | 3 |
| 10005 | Ellen | 300000 | 3 |

部署を `persist` すると、`cascade = CascadeType.ALL` により社員 2 人も一緒に登録されます。
