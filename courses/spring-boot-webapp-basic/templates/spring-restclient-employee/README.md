# spring-restclient-employee — RestClient による同期 HTTP 通信

チャプター 12.1「RestClientとRestTemplateによる同期HTTP通信」で使うサンプルです。
2 つのプログラムの組になっています。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| ルート（`src/`） | 確認用の社員 API（Spring Boot、ポート 8095、DB 不要でメモリ内に社員 16 件） | ツールバーの ▶ アプリ実行 |
| `client/` | RestClient で API を呼ぶ Java コンソール（`Main_Employee`）。処理が終わると終了する | `client/run.sh` を ▶ 実行 |

講義の中心は `client/src/main/java/pro/kensait/spring/employee/client/Main_Employee.java` です。
client は API が起動していないと接続に失敗するので、必ず API を先に起動します。

## 動かし方

1. ツールバーの ▶ アプリ実行で API を起動する（`http://127.0.0.1:8095` で待ち受ける）
2. 起動したまま `client/run.sh` を開き、エディタの右上の ▶ 実行で client を動かす
   （スクリプトはアプリの脇で動き、ルートの `gradlew.bat` で `client/` の `run` タスクを実行する）
3. 取得・検索・登録（201 と Location）・更新・削除と、削除後の 404 の出力を確かめる

ルートの `gradlew.bat` は ▶ アプリ実行のときに Codinable が作ります。
「`../gradlew.bat` がありません」と出たときは、先に手順 1 を行ってください。

接続先は、第 1 引数 → 環境変数 `EMPLOYEE_API_URL` → 既定値 `http://localhost:8095` の順で決まります。
client が変更するのは自分で登録した社員だけです。API を再起動すると初期データに戻ります。

## テスト

- `src/test/java/pro/kensait/spring/employee/api/EmployeeApiTest.java`: 確認用 API のテスト。開いてエディタの右上の ▶ 実行で流す
- `client/test.sh`: client のテスト。開いて ▶ 実行で流す（JDK の簡易 HTTP サーバーを相手にするので API の起動は不要）

サンプルの API のテストには「client の `Main_Employee` を実 API へつないで確かめる」検証もありましたが、
ここでは client をルートと別の Gradle プロジェクトにしているため、その 1 件を外しています。
