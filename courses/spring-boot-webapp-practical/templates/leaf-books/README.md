# Leaf Books（SPA 版: REST API + React）

React の画面（`frontend/`）と、JSON を返す Spring Boot の REST API
（このフォルダ直下、ポート 8080）で動く書店アプリです。
ログインと顧客登録では、外部 API の Customer Hub（ポート 8081）を呼びます。

## Codinable での動かし方

動かすプロセスは 3 つです。「実行」ボタンで動くのは REST API だけなので、
残りの 2 つはターミナルから起動します（ターミナルは複数開けます）。

1. **実行対象 `gradle:bootRun` を選んで「実行」** → REST API が `http://localhost:8080` で起動する

2. **Customer Hub をターミナルで起動する**（ログイン・顧客登録に必要）

   ```bash
   cd customer-hub
   ../gradlew.bat bootRun
   ```

   `../gradlew.bat` は手順 1 の「実行」で Codinable が用意します。
   まだ無いときは、先に手順 1 を済ませてください。

3. **React の開発サーバーを別のターミナルで起動する**

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   表示された `http://localhost:5173` をプレビューの URL 欄に入れて開きます。
   `/api` への要求は Vite が `http://localhost:8080` へ中継します（`vite.config.ts`）。

4. ログイン画面でメールアドレス `alice@gmail.com`、パスワード `password` を入れる
   （bob / carol / dave / ellen も同じパスワード）

REST API は `/api/auth/login` 以外は認証が必要です。
ブラウザで `http://localhost:8080/api/books` を直接開くと 403 になるのはそのためです。

実行対象 `gradle:test` では、Service（Mockito）・Repository（`@DataJpaTest`）・
CustomerApiClient のテストが走ります。

## curl で API を確かめる

JWT は HttpOnly Cookie（`token`）で、更新系の要求には CSRF トークン
（Cookie `XSRF-TOKEN` の値をヘッダー `X-XSRF-TOKEN` に）が要ります。

```bash
curl -s -c cj -b cj -o /dev/null http://localhost:8080/api/books   # XSRF-TOKEN を受け取る（本体は 403）
X=$(grep XSRF cj | awk '{print $7}')
curl -s -c cj -b cj -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $X" \
  -X POST http://localhost:8080/api/auth/login \
  -d '{"email":"alice@gmail.com","password":"password"}'
curl -s -b cj http://localhost:8080/api/books
curl -s -b cj http://localhost:8080/api/orders/history
```

## DB

HSQLDB のインメモリで動かしています。起動のたびに `sql/hsqldb/` の
`2_BOOKSTORE_DDL.sql` と `3_BOOKSTORE_DML.sql` から作り直されるので、
注文や在庫の変更は停止すると元に戻ります。
元の講座のように HSQLDB サーバーを起動する必要はありません。

## 構成

```
src/main/java/pro/kensait/leafbooks/
├── api/          REST Controller（auth / books / orders / images）と DTO
├── security/     JWT の発行・検証（JwtUtil・JwtAuthenticationFilter）と Spring Security の設定
├── service/      業務処理（OrderService・BookService・DeliveryFeeService など）
├── repository/   Spring Data JPA
├── entity/       JPA Entity
└── external/     Customer Hub を呼ぶ CustomerApiClient
src/main/resources/static/images/covers/   書籍の表紙画像（/api/images/covers/{id} で配信）
frontend/         React + TypeScript + Vite の画面（pages / components / contexts / services）
customer-hub/     外部 API 役の Customer Hub（別の Gradle プロジェクト）
sql/hsqldb/       起動時に流す DDL / DML
```
