# spring-graphql-employee — GraphQL による社員 CRUD

Spring for GraphQL の Query／Mutation による社員 CRUD です。GET ではなく `POST /graphql` の `query` と `variables` で操作します。部署内の社員は `@BatchMapping` でまとめて取得します。HTTP 200 でも本文の `errors` 配列を必ず確認します。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8088、`/graphql`・`/graphiql`） | ▶ アプリ実行 |
| `frontend/` | 専用の React 画面（Vite、5173） | ▶ アプリ実行で一緒に起動 |

## DB について（Codinable での変更点）

講座では HSQLDB サーバー、または `--spring.profiles.active=demo` によるインメモリ DB を使います。Codinable の実行ボタンでは引数を渡せないため、`application.yml` で **demo を既定のプロファイル**にしてあります。起動のたびにプロセス内の HSQLDB に `src/main/resources/demo/schema.sql`・`data.sql` が流れ、社員 16 件・部署 4 件から始まります。

`sql/hsqldb/` の SQL は講座の HSQLDB サーバー用の初期化スクリプトです（既存の EMPLOYEE／DEPARTMENT を作り直します）。

## 動かし方

1. ▶ アプリ実行を押します。バックエンドと React 画面（`frontend/`）が一緒に起動します。`npm install` も必要なときに自動で走ります。
2. プレビューの URL 欄に `http://localhost:8088/graphiql` を入れると、GraphiQL でクエリを対話的に試せます（例: `{ employees { id employeeName department { departmentName } } }`）。
3. 専用の React 画面を使う場合は、プレビューの URL 欄に `http://localhost:5173/` を入れます（この演習ではプレビューは GraphiQL のまま切り替わりません）。

   画面の `/api/graphql/graphql` への要求は、Vite（`frontend/vite.config.ts`）が 8088 の `/graphql` へ転送します。

   React 画面の出力は実行結果タブに `[frontend] ` を付けて出ます。「停止」ではバックエンドだけが止まり、React 画面は出力タブの行の右端にある「frontend停止」「frontend起動」で個別に操作します。

curl で確かめる場合は、▶ アプリ実行で起動したまま `curl/` のスクリプト（`01_employees.sh` 〜 `08_delete_employee.sh`）を順に開き、エディタの右上の ▶ 実行で送ります。Query（全件・主キー・存在しない場合の `errors`・部署で検索・部署から社員へのネスト）と Mutation（登録・更新・削除）の順に並んでいて、各スクリプトの先頭のコメントに期待する結果を書いてあります。

## errors の実例

どれも HTTP ステータスは 200 で、本文の `errors` で失敗が返ります（実際に送って得た応答です）。

`curl/03_employee_not_found.sh`（存在しない社員。`GraphqlErrors` の `missing` が `NOT_FOUND` に変換する。`employee` は null を許す型なので `data.employee` が null）

```json
{"errors":[{"message":"社員が存在しません => 99999","locations":[{"line":1,"column":3}],"path":["employee"],"extensions":{"classification":"NOT_FOUND"}}],"data":{"employee":null}}
```

`{ a: employee(id: 10001) { employeeName } b: employee(id: 99999) { employeeName } }`（一部だけ失敗。成功したフィールドは `data` に残る）

```json
{"errors":[{"message":"社員が存在しません => 99999","locations":[{"line":1,"column":43}],"path":["b"],"extensions":{"classification":"NOT_FOUND"}}],"data":{"a":{"employeeName":"Alice"},"b":null}}
```

`mutation { createEmployee(input: {employeeName: "", departmentId: 3, jobName: "ASSOCIATE", salary: 230000, entranceDate: "2018-04-01"}) { id } }`（入力の不備。`GraphqlErrors` の `invalid` が `BAD_REQUEST` に変換する。`createEmployee` は null を許さない型なので `data` 全体が null）

```json
{"errors":[{"message":"社員名・部署・給与・日付を確認してください","locations":[{"line":1,"column":12}],"path":["createEmployee"],"extensions":{"classification":"BAD_REQUEST"}}],"data":null}
```

`{ employee(id: 99999) { nosuchfield } }`（スキーマに無いフィールド。実行の前に検証で止まるので `data` が無い）

```json
{"errors":[{"message":"Validation error (FieldUndefined@[employee/nosuchfield]) : Field 'nosuchfield' in type 'Employee' is undefined","locations":[{"line":1,"column":25}],"extensions":{"classification":"ValidationError"}}]}
```

## 主なファイル

- `src/main/resources/graphql/schema.graphqls` … スキーマ（型・Query・Mutation）
- `src/main/java/pro/kensait/spring/employee/graphql/api/EmployeeGraphqlController.java` … リゾルバーと `@BatchMapping`
- `frontend/src/services/api.ts` … 画面からの GraphQL 要求と `errors` の検査
