# Customer Hub（外部 API 役の REST API）

顧客の登録・照会・変更・削除を HTTP API として提供する Spring Boot アプリです。
Leaf Books（MVC 版 / SPA 版）はログインと顧客登録のときに、この API を呼びます。

## Codinable での動かし方

- 実行対象 `gradle:bootRun` を選んで「実行」→ `http://localhost:8081` で起動する
- 実行対象 `gradle:test` で Controller / Service / Repository のテストが走る

DB は HSQLDB のインメモリで、起動のたびに `sql/hsqldb/` の DDL と DML
（`2_BOOKSTORE_DDL.sql` / `3_BOOKSTORE_DML.sql`）から作り直されます。
元の講座のように HSQLDB サーバーを起動する必要はありません。

## 確かめる URL

| URL | 内容 |
|---|---|
| `http://localhost:8081/customers/` | 全顧客 |
| `http://localhost:8081/customers/1` | 顧客 ID で取得 |
| `http://localhost:8081/customers/query_email?email=alice@gmail.com` | メールアドレスで照会（ログイン時に書店が使う） |
| `http://localhost:8081/customers/query_birthday?birthday=1995-01-01` | 誕生日以降の顧客 |
| `http://localhost:8081/customers/999` | 存在しない ID（404 の応答） |

ターミナルから curl でも確かめられます。

```bash
curl -i http://localhost:8081/customers/query_email?email=alice@gmail.com
curl -i -X POST http://localhost:8081/customers/ \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Frank","password":"password","email":"frank@gmail.com","birthday":"2000-01-01","address":"Tokyo"}'
```

初期データの顧客は alice / bob / carol / dave / ellen（`@gmail.com`）で、
パスワードはすべて `password` です（DB には BCrypt のハッシュが入っています）。

## 構成

```
src/main/java/pro/kensait/customer/
├── api/          CustomerApi（REST Controller）・例外ハンドラ・CustomerTO
├── service/      CustomerService と業務例外
├── repository/   CustomerRepository（Spring Data JPA）
└── entity/       Customer
sql/hsqldb/       起動時に流す DDL / DML（Codinable の SQL 実行でも中身を確かめられる）
```
