# spring-rest-employee — @RestController による社員管理 REST API

チャプター 11.1「Spring WebとSpring Data RESTによるREST API」（11.1.1〜11.1.3）で使うサンプルです。
`EmployeeApi` → `EmployeeService` → `EmployeeRepository`（JPA）の構成で、
入力 DTO の検証、`ResponseEntity` による 201 / 204、例外から 400 / 404 / 409 への変換
（`ApiExceptionHandler`）、version による更新競合を扱います。

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. 起動したまま、`curl/` のスクリプト（`01_masters.sh` 〜 `11_invalid_department.sh`）を順に開いて、
   エディタの右上の ▶ 実行で送る（マスタ・取得・一覧・検索・登録・更新・競合・削除・400）。
   各スクリプトの先頭のコメントに、期待する結果を書いてある
3. `http://localhost:8080/index.html` の確認画面でも一覧・検索・登録ができる
   （画面は React を CDN から読み込むのでインターネット接続が必要）

`src/test/java` の `EmployeeApiTest.java` を開いてエディタの右上の ▶ 実行で流すと、`EmployeeApiTest` が API をランダムポートで起動して HTTP で確かめます。

## DB について

サンプルは起動時に `--spring.profiles.active=demo` を付けて、インメモリの HSQLDB を使っていました。
Codinable の ▶ アプリ実行は引数を渡さないので、`application.yml` に `spring.profiles.default: demo` を書き、
何も指定しなければ demo（起動のたびに初期データへ戻るインメモリ DB）になるようにしてあります。
API 仕様は `swagger.yaml` にあります。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/rest/api/EmployeeApi.java` … REST API の入口
- `src/main/java/pro/kensait/spring/employee/rest/api/ApiExceptionHandler.java` … 例外から 400 / 404 / 409 への変換
- `src/main/java/pro/kensait/spring/employee/rest/service/EmployeeService.java` … 業務処理と更新競合
- `curl/` … API を呼ぶスクリプト
- `src/test/java/pro/kensait/spring/employee/rest/api/EmployeeApiTest.java` … API のテスト
