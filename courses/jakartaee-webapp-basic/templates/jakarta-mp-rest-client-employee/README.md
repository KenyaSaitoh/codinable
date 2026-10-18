# jakarta-mp-rest-client-employee — MicroProfile Rest Client による同期 HTTP 通信

チャプター 14.1「クライアントAPIによる同期HTTP通信」のうち、MicroProfile Rest Client の演習です。
呼び出す側のアプリケーション（このプロジェクト）は Payara Server に配置する WAR で、確認用の社員 API（`api/`）を
インタフェース `EmployeeApiClient` の宣言だけで呼び出します。接続先とタイムアウトは MicroProfile Config で指定します。
呼び出しの手順と結果は `jakarta-rest-client-employee`（クライアント API 版）と同じです。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/employee/client/` の下にあります。

| ファイル | 内容 |
|---|---|
| `EmployeeApiClient.java` | `@RegisterRestClient(configKey = "employee-api")` のインタフェース。`@Path`・`@GET`・`@PathParam`・`@QueryParam` で通信契約を宣言する |
| `src/main/resources/META-INF/microprofile-config.properties` | `employee-api/mp-rest/url` と、接続・読み取りのタイムアウト（ミリ秒） |
| `EmployeeClientRunner.java` | `@Inject @RestClient` で受け取ったクライアントで、取得・検索・登録・更新・削除・削除後の 404 を順に呼ぶ |
| `EmployeeNotFoundMapper.java`、`EmployeeNotFoundException.java` | `ResponseExceptionMapper` で 404 を独自の例外へ変換する |
| `RunResource.java`、`RestApplication.java` | `POST /api/run` で `EmployeeClientRunner` を動かし、記録をテキストで返す |
| `EmployeeTO.java` | 転送オブジェクト（クライアント API 版と同じ） |
| `api/` | 呼び出し先の確認用の社員 API（`employee-api.war`）。メモリー内に社員 16 件を持つ |
| `codinable.services.json` | `"wars": ["api"]` で、`api/` の WAR も同じ Payara Server に一緒にデプロイする |
| `curl/01_run.sh`、`curl/02_api_direct.sh` | 呼び出す側と、呼び出し先の API を叩くスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。確認用 API（`/employee-api`）とこのアプリケーション（`/jakarta-mp-rest-client-employee`）が一緒にデプロイされ、
   プレビューに `http://localhost:8080/jakarta-mp-rest-client-employee/` が開く。「実行」を押すと記録が表示される
2. `curl/01_run.sh` を開いて、エディタの右上の ▶ 実行で流す
3. `curl/02_api_direct.sh` で、呼び出し先の API に同じ要求を直接送ってみる

## 結果のどこを見るか

- `01_run.sh` の応答に、社員の取得・検索・登録（Location）・更新・削除・削除後の 404 の記録が順に出る
- `EmployeeApiClient` には実装クラスが無い。メソッドの呼び出しが、そのまま HTTP の要求になっている
- 削除後の `client.find(id)` は、`EmployeeNotFoundMapper` によって `EmployeeNotFoundException` になり、想定した 404 として扱われる
- 登録した社員はこの実行の中で削除するので、何度流しても同じ結果になる（ID だけが 10017、10018 … と進む）
