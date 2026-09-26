# spring-restclient-employee — RestClient による同期 HTTP 通信

チャプター 12.1「RestClientとRestTemplateによる同期HTTP通信」で使うサンプルです。
2 つのプログラムの組になっています。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| ルート（`src/`） | 確認用の社員 API（Spring Boot、ポート 8095、DB 不要でメモリ内に社員 6 件） | 「実行」ボタン（`gradle:bootRun`） |
| `client/` | RestClient で API を呼ぶ Java コンソール（`Main_Employee`）。処理が終わると終了する | ターミナル |

講義の中心は `client/src/main/java/pro/kensait/spring/employee/client/Main_Employee.java` です。
client は API が起動していないと接続に失敗するので、必ず API を先に起動します。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す（API が `http://127.0.0.1:8095` で待ち受ける）
2. ターミナル（Git Bash）で client を実行する

   ```bash
   cd client
   ../gradlew.bat run
   ```

3. 取得・検索・登録（201 と Location）・更新・削除と、削除後の 404 の出力を確かめる

`gradlew.bat` は、ルートで一度「実行」を押すと Codinable がルートに作ります。
「`../gradlew.bat` が見つからない」と出たときは、先に手順 1 を行ってください。

接続先は、第 1 引数（`../gradlew.bat run --args=http://127.0.0.1:8095`）→ 環境変数 `EMPLOYEE_API_URL`
→ 既定値 `http://localhost:8095` の順で決まります。
client が変更するのは自分で登録した社員だけです。API を再起動すると初期データに戻ります。

## テスト

- 実行対象 `gradle:test`: 確認用 API のテスト（`EmployeeApiTest`）
- ターミナルで `cd client` → `../gradlew.bat test`: client のテスト（JDK の簡易 HTTP サーバーを相手にするので API の起動は不要）

サンプルの API のテストには「client の `Main_Employee` を実 API へつないで確かめる」検証もありましたが、
ここでは client をルートと別の Gradle プロジェクトにしているため、その 1 件を外しています。
