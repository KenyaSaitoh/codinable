# spring-graphql-employee — GraphQL による社員 CRUD

Spring for GraphQL の Query／Mutation による社員 CRUD です。GET ではなく `POST /graphql` の `query` と `variables` で操作します。部署内の社員は `@BatchMapping` でまとめて取得します。HTTP 200 でも本文の `errors` 配列を必ず確認します。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8088、`/graphql`・`/graphiql`） | 実行対象 `gradle:bootRun` で「実行」 |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |

## DB について（Codinable での変更点）

講座では HSQLDB サーバー、または `--spring.profiles.active=demo` によるインメモリ DB を使います。Codinable の実行ボタンでは引数を渡せないため、`application.yml` で **demo を既定のプロファイル**にしてあります。起動のたびにプロセス内の HSQLDB に `src/main/resources/demo/schema.sql`・`data.sql` が流れ、社員 6 件・部署 3 件から始まります。

`sql/hsqldb/` の SQL は講座の HSQLDB サーバー用の初期化スクリプトです（既存の EMPLOYEE／DEPARTMENT を作り直します）。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押します。
2. プレビューの URL 欄に `http://localhost:8088/graphiql` を入れると、GraphiQL でクエリを対話的に試せます（例: `{ employees { id employeeName department { departmentName } } }`）。
3. 専用の React 画面を使う場合は、ターミナルで起動し、プレビューの URL 欄に `http://localhost:5173/` を入れます。

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   画面の `/api/graphql/graphql` への要求は、Vite（`frontend/vite.config.ts`）が 8088 の `/graphql` へ転送します。

curl の例は `curlメモ.txt` にあります（Windows のコマンドプロンプト向けの書き方です）。Codinable のターミナル（bash）では JSON を単一引用符で囲みます。

```bash
curl -i -X POST http://localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ employee(id: 1) { id employeeName department { departmentName } } }"}'
```

## 主なファイル

- `src/main/resources/graphql/schema.graphqls` … スキーマ（型・Query・Mutation）
- `src/main/java/pro/kensait/spring/employee/graphql/api/EmployeeGraphqlController.java` … リゾルバーと `@BatchMapping`
- `frontend/src/services/api.ts` … 画面からの GraphQL 要求と `errors` の検査
