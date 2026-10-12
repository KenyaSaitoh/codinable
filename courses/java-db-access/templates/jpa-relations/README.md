# jpa-relations — 双方向の関連・fetch join・Criteria（導入・復習用）

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-relations` を、Codinable で単独で動くようにしたものです。

部署と社員の双方向の関連を持つ小さな教材です。関連の所有側・mappedBy・cascade・fetch はそれぞれ別の役割です。

## 読む順番

1. `Department` と `Employee` で、`@OneToMany(mappedBy)` と `@ManyToOne` の所有側、`orphanRemoval=true` を確認する。
2. `Department#addEmployee` が両方向の参照をそろえることを確認する。
3. `CompanyQuery` の fetch join と、Criteria API による給与条件の検索を読む。
4. `CompanyQueryTest` で、取得結果と発行される SQL を確認する。

`CascadeType.ALL` や EAGER は効果を比べるための設定で、全関連に適用する推奨値ではありません。
コレクションの fetch join とページングを同時に使うときは注意が要ります。
この Criteria は文字列で属性を指定するため、完全なコンパイル時の型安全性はありません。

## Codinable での動かし方

- テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。
- `CompanyQueryTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。結果はテスト結果タブにメソッド単位で出ます。
- テストはプロジェクト専用のインメモリ DB（`jdbc:hsqldb:mem:jpa_relations`）を自分で作って使うので、DB サーバーの起動は要りません。
- このプロジェクトに main はありません（テストが入口です）。

<!-- schema:begin -->
## この演習で使うテーブル

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

### DEPARTMENT（部署）

| カラム | Java のフィールド | 制約 |
|---|---|---|
| `ID` | `Integer id` | 主キー |
| `NAME` | `String name` | |

`employees`（`@OneToMany(mappedBy = "department")`）はカラムを持ちません。
関連の持ち主は EMPLOYEE 側の `DEPARTMENT_ID` です。

### EMPLOYEE（社員）

| カラム | Java のフィールド | 制約 |
|---|---|---|
| `ID` | `Integer id` | 主キー |
| `NAME` | `String name` | |
| `SALARY` | `BigDecimal salary` | |
| `DEPARTMENT_ID` | `Department department` | 外部キー → `DEPARTMENT.ID`、NOT NULL（`optional = false`） |

### テストが登録するデータ（`CompanyQueryTest.java`）

| DEPARTMENT.ID | DEPARTMENT.NAME |
|---|---|
| 3 | SALES |

| EMPLOYEE.ID | NAME | SALARY | DEPARTMENT_ID |
|---|---|---|---|
| 10001 | Alice | 500000 | 3 |
| 10005 | Ellen | 300000 | 3 |

部署を `persist` すると、`cascade = CascadeType.ALL` により社員 2 人も一緒に登録されます。
<!-- schema:end -->
