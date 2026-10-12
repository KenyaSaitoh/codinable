# spring-webclient-employee — WebClient と確認用 API

WebClient の Mono／Flux と購読を学ぶ Java コンソール（`client/`）と、呼び出し先の確認用 Spring Boot API（このフォルダ直下）の組です。DB や他章のアプリは不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | 確認用 API（`rest-employee-webclient-api`、127.0.0.1:8087） | ▶ アプリ実行 |
| `client/` | WebClient の Java コンソール（`rest-client-employee-webclient`） | `client/run.sh` を開いてエディタの右上の ▶ 実行 |

## 動かし方

1. ▶ アプリ実行を押し、API を起動します。プレビューに `http://127.0.0.1:8087/employees`（初期社員 16 件）が開きます。
2. API を起動したまま、`client/run.sh` を開いてエディタの右上の ▶ 実行を押します。API は止まらず、その脇でクライアントが動きます。

   社員の取得・検索に続いて、確認用社員の登録・更新・削除と、削除後の 404 が実行結果に表示されて終了します。
3. API は「停止」で止めます。データはメモリ内にあり、再起動で初期化されます。

`client/` には Gradle Wrapper が無いので、`client/run.sh` はルートの `gradlew.bat`（▶ アプリ実行のときに Codinable が作る）を使います。

接続先は、第 1 引数（`run --args='http://127.0.0.1:8087'`）、環境変数 `EMPLOYEE_API_URL`、既定 URL `http://localhost:8087` の順で決まります。クライアントは待受ポートを使いません。自分で削除した社員の 404 以外のエラーでは、実行が非ゼロで終了します。

## テスト

- `src/test/java/pro/kensait/spring/employee/api/EmployeeApiTest.java` を開いて、エディタの右上の ▶ 実行で流してください。API のテストです。Spring Boot をランダムポートで起動し、`client/` のクライアントからの接続も確かめます（ビルド設定で `client/src/main/java` をテスト用ソースとして読み込んでいます）。
- `client/test.sh` を開いて ▶ 実行: クライアントのテスト（`ClientOptionsTest`・`ConsoleClientTest`）。JDK のローカル HTTP サーバーを相手にします。

## 主なファイル

- `client/src/main/java/pro/kensait/spring/employee/client/Main_Employee.java` … WebClient の入口（retrieve、onStatus、toStream、subscribe）
- `src/main/java/pro/kensait/spring/employee/api/EmployeeController.java` … 確認用 API（CRUD・検索・400／404）
