# Leaf Books（MVC 版）

Spring MVC + Thymeleaf でサーバーが HTML を生成する書店アプリです（ポート 8080）。
書籍の検索、カート、ログイン、注文、在庫更新、注文履歴までを扱います。
ログインと顧客登録では、外部 API の Customer Hub（ポート 8081）を呼びます。

## Codinable での動かし方

1. **Customer Hub をターミナルで起動する**（ログイン・顧客登録に必要）

   ```bash
   cd customer-hub
   ../gradlew.bat bootRun
   ```

   `../gradlew.bat` は、このフォルダで一度「実行」を押すと Codinable が用意します。
   まだ無いときは、先に手順 2 の「実行」を押してから起動してください。

2. **実行対象 `gradle:bootRun` を選んで「実行」**
   → 書店が `http://localhost:8080` で起動し、プレビュータブが開く

3. ログイン画面でメールアドレス `alice@gmail.com`、パスワード `password` を入れる
   （bob / carol / dave / ellen も同じパスワード）

Customer Hub を起動していないと、ログインで接続エラーになります。
書籍の一覧や検索はログイン後の画面から使えます。

実行対象 `gradle:test` では、Controller（MockMvc）・Service（Mockito）・
Repository（`@DataJpaTest`）のテストが走ります。

## DB

HSQLDB のインメモリで動かしています。起動のたびに `sql/hsqldb/` の
`2_BOOKSTORE_DDL.sql` と `3_BOOKSTORE_DML.sql` から作り直されるので、
注文や在庫の変更は停止すると元に戻ります。
元の講座のように HSQLDB サーバーを起動する必要はありません。

書店と Customer Hub は別のプロセスなので、DB も別々です
（書店の DB に顧客テーブルは無く、顧客情報は必ず API 経由で取得します）。

## 主な画面と URL

| URL | 画面 |
|---|---|
| `/` | ログイン |
| `/toSelect` | 書籍の一覧（カートに追加） |
| `/toSearch` | 書籍の検索（JPQL / Criteria） |
| `/viewCart` | カート（数量の変更・注文へ） |
| `/viewHistory` | 注文履歴 |
| `/toRegister` | 顧客登録（Customer Hub へ登録） |

## 構成

```
src/main/java/pro/kensait/leafbooks/
├── web/          Controller と画面用のフォーム・セッション（book / cart / customer / login / order）
├── service/      業務処理（OrderService・BookService・DeliveryFeeService など）
├── repository/   Spring Data JPA（JPQL・条件付き更新・FETCH JOIN）
├── entity/       JPA Entity（OrderDetail は複合主キー）
├── external/     Customer Hub を呼ぶ CustomerApiClient
└── security/     Spring Security の設定
src/main/resources/
├── templates/    Thymeleaf テンプレート
└── static/       CSS と書籍の表紙画像
customer-hub/     外部 API 役の Customer Hub（別の Gradle プロジェクト）
sql/hsqldb/       起動時に流す DDL / DML
```
