# spring-data-rest-employee — Spring Data REST による Repository の自動公開

チャプター 11.1「Spring WebとSpring Data RESTによるREST API」（11.1.4・11.1.5）で使うサンプルです。
自作の CRUD Controller・Service は置かず、`@RepositoryRestResource` を付けた
`EmployeeRepository` を Spring Data REST が `/api` の下に公開します。

| 確かめること | 場所 |
|---|---|
| 公開範囲・参照専用のマスター・CORS | `config/RestConfig.java` |
| 保存前の Bean Validation | `config/ValidatorConfig.java` |
| 保存前イベントで氏名の前後空白を除く | `config/EmployeeEventHandler.java` |
| `@Version` と ETag / If-Match | `entity/Employee.java` |
| Projection（`?projection=summary`） | `entity/EmployeeSummary.java` |

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. 起動したまま、`curl/` のスクリプトを順に開いて、エディタの右上の ▶ 実行で送る
   - `01_api_root.sh` … `http://localhost:8080/api` で 3 つの Repository へのリンク
   - `02_paging.sh`・`03_masters.sh` … HAL の `_embedded`・`_links`・`page`
   - `04_search.sh`・`05_projection.sh` … `/search` 配下の検索メソッドと Projection
   - `06_create_employee.sh` … Controller なしの POST（本文は `client/employee-create.json`）
   - `07_etag.sh` … ETag を If-Match に付けた更新と、古い ETag での 412
   - `08_not_exposed.sh` … 非公開の検索メソッドは 404、マスターへの書き込みは 405

   PUT・DELETE・304 まで含めた一連の例は `client/requests.http` にある

### 確認用の画面（client/）を使う場合

`client/src` は API の応答を観察するための補助画面です（npm も外部 CDN も使わない、素の HTML / JavaScript）。
API は `http://localhost:5500` と `http://127.0.0.1:5500` からのアクセスだけを CORS で許可しているので、
5500 番で配信します。

1. ▶ アプリ実行で API を起動したまま、`client/serve.sh` を開いてエディタの右上の ▶ 実行で配信を始める
   （`python -m http.server 5500 --bind 127.0.0.1 --directory client/src` がアプリの脇で動き続ける）
2. プレビューの URL に `http://127.0.0.1:5500` を入れて開く

配信は「停止」で止まります。別のスクリプト（`curl/` など）を ▶ 実行したときも止まるので、
画面を使うときは curl のスクリプトと交互に使わず、必要になったら `client/serve.sh` を動かし直してください。

## テスト

`src/test/java/pro/kensait/spring/employee/EmployeeDataRestTest.java` を開いてエディタの右上の ▶ 実行で流すと、
生成された HTTP API を実 DB 込みで確かめます。

## DB について

サンプルは起動時に `--spring.profiles.active=demo` を付けて、インメモリの HSQLDB を使っていました。
Codinable の ▶ アプリ実行は引数を渡さないので、`application.yml` に `spring.profiles.default: demo` を書き、
何も指定しなければ demo（起動のたびに初期データへ戻るインメモリ DB）になるようにしてあります。
