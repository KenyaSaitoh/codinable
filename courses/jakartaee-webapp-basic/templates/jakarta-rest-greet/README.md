# jakarta-rest-greet — リソースクラスと入力の受け取り

チャプター 13.1「Jakarta RESTによるREST API」の最初のサンプル（挨拶 API）です。
`@ApplicationPath("/api")` の `RestApplication` と、`@Path("/greet")` の `GreetResource` だけで、
同じ名前の値をパス・クエリ・フォーム・JSON の本文・ヘッダーのそれぞれから受け取ります。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/greet/rest/RestApplication.java` | REST API の URL の起点（`/api`） |
| `src/main/java/pro/kensait/jakartaee/greet/rest/GreetResource.java` | `@PathParam`・`@QueryParam`・`@FormParam`・本文・`@HeaderParam` で受け取り、`Response.ok(...)` で返す |
| `src/main/java/pro/kensait/jakartaee/greet/rest/GreetingRequest.java` | JSON の本文を受け取るレコード |
| `curl/01_path_param.sh` 〜 `curl/05_header.sh` | 入力の場所ごとに要求を送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-rest-greet/api/greet/hello/Alice` が開く
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で 1 つずつ流す

## 結果のどこを見るか

| メソッドとパス | 入力 | アノテーション |
|---|---|---|
| GET /api/greet/hello/Alice、/goodbye/Alice | パスの Alice | `@PathParam` |
| GET /api/greet/morning?name=Alice | クエリの name | `@QueryParam` |
| POST /api/greet/afternoon | フォームの name | `@FormParam` |
| POST /api/greet/evening | JSON 本文の name | なし（本文） |
| GET /api/greet/night | name ヘッダー | `@HeaderParam` |

- URL は「コンテキストルート（`/jakarta-rest-greet`）＋ `@ApplicationPath`（`/api`）＋ クラスとメソッドの `@Path`」になる
- `03_form.sh` の 2 つ目は、フォーム用の `/afternoon` に JSON を送るので、`@Consumes` と一致せず 415 になる
