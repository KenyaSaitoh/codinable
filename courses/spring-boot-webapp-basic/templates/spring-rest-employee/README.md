# spring-rest-employee — @RestController による社員管理 REST API

チャプター 11.1「Spring WebとSpring Data RESTによるREST API」（11.1.1〜11.1.3）で使うサンプルです。
`EmployeeApi` → `EmployeeService` → `EmployeeRepository`（JPA）の構成で、
入力 DTO の検証、`ResponseEntity` による 201 / 204、例外から 400 / 404 / 409 への変換
（`ApiExceptionHandler`）、version による更新競合を扱います。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. ターミナル（Git Bash）で `curlメモ.txt` の curl を順に送る（一覧・取得・検索・登録・更新・競合・削除）
3. `http://localhost:8080/index.html` の確認画面でも一覧・検索・登録ができる
   （画面は React を CDN から読み込むのでインターネット接続が必要）

実行対象を `gradle:test` にすると、`EmployeeApiTest` が API をランダムポートで起動して HTTP で確かめます。

## DB について

サンプルは起動時に `--spring.profiles.active=demo` を付けて、インメモリの HSQLDB を使っていました。
Codinable の「実行」は引数を渡さないので、`application.yml` に `spring.profiles.default: demo` を書き、
何も指定しなければ demo（起動のたびに初期データへ戻るインメモリ DB）になるようにしてあります。
API 仕様は `swagger.yaml` にあります。
