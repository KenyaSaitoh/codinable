# customer-hub — 外部 API の Customer Hub（Jakarta REST + Jakarta Persistence）

書店 Leaf Books から呼ばれる側（プロバイダー）の顧客 API です。顧客の照会・登録などを JSON で提供します。
書店の演習（leaf-books）では同じ Payara Server に一緒にデプロイされますが、ここでは Customer Hub だけを動かします。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/backend/src/main/java/pro/kensait/customerhub/resource/CustomerResource.java` | `/customers` のリソースクラス（ID・メールアドレスでの照会、登録） |
| `src/backend/src/main/java/pro/kensait/customerhub/service/CustomerService.java` | 登録済みのメールアドレスなら `CustomerExistsException` を送出する |
| `src/backend/src/main/java/pro/kensait/customerhub/resource/CustomerExceptionMapper.java` | 業務例外を 404 / 409 / 500 の応答に変換する |
| `src/backend/src/main/java/pro/kensait/customerhub/dao/CustomerDao.java` | `EntityManager` による検索と保存 |

## 動かし方

1. ▶ アプリ実行を押します。Gradle が WAR を作り、同梱の Payara Server 7 にデプロイします
2. デプロイが終わると、プレビューに `http://localhost:8080/customer-hub-api/customers/1` が開き、顧客 1（Alice）の JSON が出ます
3. ▶ アプリ実行で起動したまま `curl/` のスクリプトを開き、エディタの右上の ▶ 実行で送ります

| スクリプト | 確かめること |
|---|---|
| `curl/01_get_customer.sh` | ID とメールアドレスでの照会は 200、登録されていないメールアドレスは 404（`customer.not-found`） |
| `curl/02_register_customer.sh` | 登録は 200、同じメールアドレスでもう一度登録すると 409（`customer.exists`） |

書店のログイン（`CustomerHubRestClient`）は、メールアドレスでの照会の 200 と 404 を「顧客がいる／いない」として扱います。
▶ アプリ実行のたびにテーブルと初期データを作り直すので、登録した顧客は次の実行には残りません。

## Codinable での変更点

- データソース `jdbc/HsqldbDS` は、`payara/post-boot-commands.asadmin` で同梱の HSQLDB のインメモリ DB に登録します
- テーブルと初期データは、デプロイのたびに `persistence.xml` のスキーマ生成が `src/backend/sql/hsqldb/` の SQL から作ります（`build.gradle` の `flattenSql` が 1 行 1 文の形に整えて WAR に入れます）

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「外部APIのCustomer Hubを動かす」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `src/backend/sql/hsqldb/2_CUSTOMER_HUB_DDL.sql`、初期データ: `src/backend/sql/hsqldb/3_CUSTOMER_HUB_DML.sql`）。

### CUSTOMER

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `CUSTOMER_ID` | `INT` | 主キー、自動採番 | 顧客ID |
| `CUSTOMER_NAME` | `VARCHAR(50)` | NOT NULL | 顧客名 |
| `PASSWORD` | `VARCHAR(60)` | NOT NULL | パスワード |
| `EMAIL` | `VARCHAR(200)` | UNIQUE、NOT NULL | Eメールアドレス |
| `BIRTHDAY` | `DATE` |  | 生年月日 |
| `ADDRESS` | `VARCHAR(200)` |  | 住所 |

初期データ（5 件）

| CUSTOMER_ID | CUSTOMER_NAME | PASSWORD | EMAIL | BIRTHDAY | ADDRESS |
|---|---|---|---|---|---|
| 1 | Alice | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | alice@example.com | 1998-04-10 | 東京都中央区1-1-1 |
| 2 | Bob | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | bob@example.com | 1988-05-10 | 東京都杉並区2-2-2 |
| 3 | Carol | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | carol@example.com | 1993-06-10 | 東京都文教区3-3-3 |
| 4 | Dave | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | dave@example.com | 1990-07-10 | 東京都品川区4-4-4 |
| 5 | Ellen | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | ellen@example.com | 1999-08-10 | 東京都中野区5-5-5 |
<!-- schema:end -->
