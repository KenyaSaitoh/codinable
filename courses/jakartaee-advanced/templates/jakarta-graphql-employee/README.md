# jakarta-graphql-employee — MicroProfile GraphQL のスキーマとデータ取得

チャプター 6.1「MicroProfile GraphQLのスキーマとデータ取得」の演習です。
Java のクラスとアノテーションからスキーマを生成するコードファーストの GraphQL API で、社員と部署を問い合わせ・変更します。
Payara Server 7 は MicroProfile GraphQL の実装を同梱しないので、実装の SmallRye GraphQL を WAR に含めています。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | GraphQL API（`http://localhost:8080/jakarta-graphql-employee/graphql`） | ▶ アプリ実行 |
| `frontend/` | React の画面（Vite、5173）。`/graphql` を 8080 の `/jakarta-graphql-employee/graphql` へ転送する | ▶ アプリ実行で一緒に起動 |

社員と部署は Jakarta Persistence のエンティティで、同梱の HSQLDB（インメモリ）に入っています。デプロイのたびに初期データへ戻ります。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/graphql/entity/Employee.java` | エンティティ兼 GraphQL の型（`@Name("id")`・`@Id`・`@NonNull`） |
| `src/main/java/pro/kensait/jakartaee/employee/graphql/api/EmployeeGraphQLApi.java` | `@GraphQLApi`・`@Query`・`@Mutation`、`@Source List<Department>` によるバッチ取得 |
| `src/main/java/pro/kensait/jakartaee/employee/graphql/api/EmployeeInput.java` | 入力型（`@Input("EmployeeInput")`） |
| `src/main/java/pro/kensait/jakartaee/employee/graphql/service/EmployeeService.java` | 業務処理（Bean Validation・保存）と、部署をまとめて検索する JPQL |
| `frontend/src/components/GraphqlPanel.tsx`・`frontend/src/services/api.ts` | 画面から送るクエリと、`errors` の確認 |
| `sql/hsqldb/` | テーブル定義と初期データ（`build.gradle` の `flattenSql` が WAR に入れる） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。GraphQL API と React の画面（5173）が起動し、プレビューに画面が開く
2. 画面で「詳細」「部署と所属社員」の「実行」「登録」「削除」を試す。どれも同じ `/graphql` へ、選ぶ項目の違うクエリを送っている
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で順に流す
   - `01_employee.sh` … 変数付きの社員取得、存在しない社員の `errors`、スキーマに無いフィールドの検証エラー
   - `02_departments.sh` … 部署と所属社員（バッチ取得）
   - `03_mutations.sh` … 登録（10017 が採番される）・存在しない部署・更新・削除・存在しない日付
   - `04_schema.sh` … 生成されたスキーマ（`/graphql/schema.graphql`）

## 結果のどこを見るか

- 存在しない社員は HTTP 200 のまま、`errors` の `message` に「社員が存在しません => 99999」が入り、`data.employee` は `null` になる。HTTP のステータスだけで成否を判断しない
- `02_departments.sh` のあと、実行結果に `SELECT ... FROM DEPARTMENT` が 1 回、`[ EmployeeService#employeesByDepartments ] departments=4` と
  `... DEPARTMENT_ID IN (?,?,?,?)` の SELECT が 1 回だけ出る（部署 4 件の社員を 1 回の問い合わせでまとめて取得している）
- `EmployeeGraphQLApi` のバッチ版の `employees` をコメントアウトし、部署ごとに呼ばれる版のコメントを外して ▶ アプリ実行し直すと、
  同じクエリで `getEmployeesByDepartment` と社員の SELECT が部署の数（4 回）だけ出る（N+1 問題）
- `@Description` の説明文や `@NonNull` の有無が、そのまま `04_schema.sh` のスキーマに表れる
