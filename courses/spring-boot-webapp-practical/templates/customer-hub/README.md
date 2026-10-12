# Customer Hub（外部 API 役の REST API）

顧客の登録・照会・変更・削除を HTTP API として提供する Spring Boot アプリです。
Leaf Books（MVC 版 / SPA 版）はログインと顧客登録のときに、この API を呼びます。

## Codinable での動かし方

- ツールバーの **▶ アプリ実行** を押す → `http://localhost:8081` で起動し、プレビューで `/customers/1` が開く
- テストは `src/test/java/pro/kensait/customer/` の `api/CustomerApiTest.java`・`service/CustomerServiceTest.java`・
  `repository/CustomerRepositoryTest.java` を開いて、エディタの右上の ▶ 実行で流す

主に読むのは `src/main/java/pro/kensait/customer/api/CustomerApi.java`（REST Controller）です。
`CustomerService` を呼び、業務例外を例外ハンドラで HTTP ステータス（404 など）へ変換する流れを追います。

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

curl でも確かめられます。起動したまま `curl/` のスクリプトを順に開いて、エディタの右上の ▶ 実行で送ります
（`01_get_customer.sh` が ID での取得と 404、`02_query_email.sh` がメールアドレスでの照会、`03_create_customer.sh` が顧客の登録）。

初期データの顧客は alice / bob / carol / dave / ellen（`@gmail.com`）で、
パスワードはすべて `password` です（DB には BCrypt のハッシュが入っています）。

## 構成

```
src/main/java/pro/kensait/customer/
├── api/          CustomerApi（REST Controller）・例外ハンドラ・CustomerTO
├── service/      CustomerService と業務例外
├── repository/   CustomerRepository（Spring Data JPA）
└── entity/       Customer
sql/hsqldb/       起動時に流す DDL / DML
```
