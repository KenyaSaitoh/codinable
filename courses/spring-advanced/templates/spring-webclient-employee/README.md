# spring-webclient-employee — WebClient と確認用 API

WebClient の Mono／Flux と購読を学ぶ Java コンソール（`client/`）と、呼び出し先の確認用 Spring Boot API（このフォルダ直下）の組です。DB や他章のアプリは不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | 確認用 API（`rest-employee-webclient-api`、127.0.0.1:8087） | 実行対象 `gradle:bootRun` で「実行」 |
| `client/` | WebClient の Java コンソール（`rest-client-employee-webclient`） | ターミナルで `../gradlew.bat run` |

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押し、API を起動します。`http://127.0.0.1:8087/employees` で初期社員 6 件が返ります。
2. ターミナルでクライアントを実行します。

   ```bash
   cd client
   ../gradlew.bat run
   ```

   社員の取得・検索に続いて、確認用社員の登録・更新・削除と、削除後の 404 がコンソールに表示されて終了します。
3. API は「停止」で止めます。データはメモリ内にあり、再起動で初期化されます。

`../gradlew.bat` は、ルートで一度「実行」を押すと Codinable が作ります。見つからないときは先に手順 1 を行ってください。

接続先は、第 1 引数（`../gradlew.bat run --args='http://127.0.0.1:8087'`）、環境変数 `EMPLOYEE_API_URL`、既定 URL `http://localhost:8087` の順で決まります。クライアントは待受ポートを使いません。自分で削除した社員の 404 以外のエラーでは、実行が非ゼロで終了します。

## テスト

- 実行対象 `gradle:test`: API のテスト。Spring Boot をランダムポートで起動し、`client/` のクライアントからの接続も確かめます（ビルド設定で `client/src/main/java` をテスト用ソースとして読み込んでいます）。
- `cd client` → `../gradlew.bat test`: クライアントのテスト。JDK のローカル HTTP サーバーを相手にします。

## 主なファイル

- `client/src/main/java/pro/kensait/spring/employee/client/Main_Employee.java` … WebClient の入口（retrieve、onStatus、toStream、subscribe）
- `src/main/java/pro/kensait/spring/employee/api/EmployeeController.java` … 確認用 API（CRUD・検索・400／404）
