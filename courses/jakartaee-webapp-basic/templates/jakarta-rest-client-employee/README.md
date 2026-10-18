# jakarta-rest-client-employee — Jakarta REST のクライアント API による同期 HTTP 通信

チャプター 14.1「クライアントAPIによる同期HTTP通信」のうち、Jakarta REST のクライアント API の演習です。
2 つのプログラムからなります。

- ルート: 呼び出し先の確認用の社員 API（`employee-api.war`）。メモリー内に社員 16 件を持ち、▶ アプリ実行で Payara Server にデプロイする
- `client/`: 確認用 API を呼び出す Java のコンソール `Main_Employee`。`client/run.sh` で動かす

## 見るファイル

| ファイル | 内容 |
|---|---|
| `client/src/main/java/pro/kensait/jakartaee/employee/client/Main_Employee.java` | `ClientBuilder` のタイムアウト、`WebTarget` の `path`・`queryParam`・`resolveTemplate`、`GenericType`、`Entity.json`、`NotFoundException` の捕捉 |
| `client/src/main/java/pro/kensait/jakartaee/employee/client/ClientOptions.java`、`EmployeeTO.java` | 接続先の決め方（引数 → 環境変数 `EMPLOYEE_API_URL` → 既定値）と転送オブジェクト |
| `client/build.gradle` | Java SE で動かすので、クライアント API の実装（Jersey）と JSON Binding の実装を含める |
| `client/run.sh` | コンソールを動かすスクリプト |
| `src/main/java/pro/kensait/jakartaee/employee/api/` | 確認用の社員 API（`/employee-api/api/employees`） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。確認用 API が `http://localhost:8080/employee-api/api` にデプロイされ、
   プレビューに `http://localhost:8080/employee-api/api/employees` の JSON が開く
2. `client/run.sh` を開いて、エディタの右上の ▶ 実行で流す（API を動かしたまま、脇でコンソールが動いて終わる）

## 結果のどこを見るか

- `Employee => …`（型を指定した `get`）と、`Status => 200`・`Headers => …`（`Response` で受け取った応答全体）
- `Department 3 => [Alice, Dave, Ellen, Matilda, Peggy]`、`Salary 300000-400000 => […]`（`queryParam` で組み立てた検索）
- `Location => …/employee-api/api/employees/10017`（201 の確認）、`Updated => …`、`Deleted => 204`
- `Expected error => 404` と、その本文。削除済みの社員の取得で、404 の `NotFoundException` だけを捕捉している
- 登録した社員はこの実行の中で削除するので、何度流しても同じ結果になる（ID だけが進む）
