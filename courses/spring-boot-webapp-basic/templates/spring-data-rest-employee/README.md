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

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. ターミナル（Git Bash）で `client/curlメモ.txt` の curl を送る（`curl.exe` は `curl` に読み替える）
   - `http://localhost:8080/api` で 3 つの Repository へのリンク
   - `http://localhost:8080/api/employees?sort=employeeId,asc` で HAL とページ情報

### 確認用の画面（client/）を使う場合

`client/src` は API の応答を観察するための補助画面です（npm も外部 CDN も使わない、素の HTML / JavaScript）。
API は `http://localhost:5500` と `http://127.0.0.1:5500` からのアクセスだけを CORS で許可しているので、
5500 番で配信します。API を「実行」で起動したまま、ターミナルで次を実行します。

```bash
python -m http.server 5500 --bind 127.0.0.1 --directory client/src
```

プレビューの URL に `http://127.0.0.1:5500` を入れて開きます（止めるときはターミナルで Ctrl+C）。
実行対象の `static:client/src` で配信すると API の実行が止まり、ポートも 5500 にならないので使いません。

実行対象を `gradle:test` にすると、`EmployeeDataRestTest` が生成された HTTP API を実 DB 込みで確かめます。

## DB について

サンプルは起動時に `--spring.profiles.active=demo` を付けて、インメモリの HSQLDB を使っていました。
Codinable の「実行」は引数を渡さないので、`application.yml` に `spring.profiles.default: demo` を書き、
何も指定しなければ demo（起動のたびに初期データへ戻るインメモリ DB）になるようにしてあります。
