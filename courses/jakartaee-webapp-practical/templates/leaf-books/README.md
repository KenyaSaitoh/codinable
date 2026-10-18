# leaf-books — オンライン書店 Leaf Books（Jakarta REST + React）

オンライン書店 Leaf Books の SPA です。書籍の検索・カート・ログイン・注文・在庫更新を、
Jakarta REST の書店 API と React の画面で動かします。顧客情報は外部 API の Customer Hub から取得します。
この雛形はチャプター2〜5の演習で共通に使い、チャプターごとに別の観点で読みます。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| `src/backend/` | 書店 API（WAR `leaf-books-api`、`http://localhost:8080/leaf-books-api/api/...`） | ▶ アプリ実行 |
| `customer-hub/src/backend/` | 外部 API の Customer Hub（WAR `customer-hub-api`） | ▶ アプリ実行で同じ Payara Server に一緒にデプロイ |
| `src/frontend/` | React の画面（Vite、5173。`/api` を書店 API へ中継） | ▶ アプリ実行で一緒に起動 |

## 動かし方

1. ▶ アプリ実行を押します。Gradle が 2 つの WAR を作り、同梱の Payara Server 7 に Customer Hub → 書店 API の順にデプロイします。React の画面も一緒に起動します（`npm install` も必要なときに自動で走ります）
2. 画面の用意ができると、プレビューに `http://localhost:5173/` が開きます。`alice@example.com` / `password` でログインし、カートに入れて注文すると、送料と注文履歴が確かめられます
3. API を直接呼ぶときは、▶ アプリ実行で起動したまま `curl/` のスクリプトを開き、エディタの右上の ▶ 実行で送ります。各スクリプトの先頭のコメントに、期待する結果を書いてあります

▶ アプリ実行のたびにサーバーを起動し直し、テーブルと初期データも作り直します。注文や在庫の更新は次の実行には残りません。

## チャプターごとに見るところ

### チャプター2 業務処理のレイヤー設計とデータ契約

| ファイル | 内容 |
|---|---|
| `src/backend/src/main/java/pro/kensait/leafbooks/api/OrderResource.java` | 注文の入口。HTTP の入力・認証情報の参照・DTO 変換 |
| `src/backend/src/main/java/pro/kensait/leafbooks/service/order/OrderService.java` | 在庫確認・在庫更新・注文と明細の保存（Transaction Script） |
| `src/backend/src/main/java/pro/kensait/leafbooks/service/order/OrderTO.java` | 注文の入力を運ぶ DTO |
| `src/backend/src/main/java/pro/kensait/leafbooks/service/delivery/DeliveryFeeService.java` | 送料の決定（800 円・沖縄県 1700 円・5000 円以上は無料） |
| `src/backend/src/test/java/pro/kensait/leafbooks/architecture/LayeredArchitectureTest.java` | ArchUnit によるレイヤーの依存方向の検査 |

テストクラスを開いて、エディタの右上の ▶ 実行で流します。`DeliveryFeeServiceTest` は送料の分岐を、
`OrderServiceTest` は注文の業務処理をモックで確かめます。結果はテスト結果タブに出ます。

### チャプター3 永続化とトランザクション、外部連携の境界

| ファイル | 内容 |
|---|---|
| `src/backend/src/main/java/pro/kensait/leafbooks/entity/OrderDetail.java`・`OrderDetailPK.java` | `@IdClass` による複合主キー |
| `src/backend/src/main/java/pro/kensait/leafbooks/dao/BookDao.java` | JPQL と Criteria API による検索 |
| `src/backend/src/main/java/pro/kensait/leafbooks/api/StockResource.java` | version を照合する在庫の更新（409） |
| `src/backend/src/main/java/pro/kensait/leafbooks/external/CustomerHubRestClient.java` | Customer Hub の呼び出しと失敗の扱い |
| `src/backend/src/main/java/pro/kensait/leafbooks/dao/OrderTranDao.java` | DTO 投影と JOIN FETCH |

| スクリプト | 確かめること |
|---|---|
| `curl/01_search_books.sh` | JPQL と Criteria API に同じ条件を渡して、同じ書籍が返る |
| `curl/04_update_stock.sh` | 読み取った version での更新は 200、同じ version でもう一度更新すると 409 |

### チャプター4 Reactの画面構成とREST APIの契約

| ファイル | 内容 |
|---|---|
| `src/frontend/vite.config.ts` | `/api` を `http://localhost:8080/leaf-books-api` へ中継する proxy |
| `src/frontend/src/services/api.ts` | Axios の共通設定（`baseURL` は `/api`、`withCredentials`） |
| `src/frontend/src/contexts/CartContext.tsx` | カートの状態と、その寿命 |
| `src/backend/src/main/java/pro/kensait/leafbooks/api/dto/OrderRequest.java` | 画面から送る注文の JSON の契約 |

### チャプター5 JWT認証と非同期処理の状態管理

| ファイル | 内容 |
|---|---|
| `src/backend/src/main/java/pro/kensait/leafbooks/api/AuthResource.java` | ログインで JWT の Cookie と CSRF トークンの Cookie を返す |
| `src/backend/src/main/java/pro/kensait/leafbooks/security/JwtAuthFilter.java` | 公開パスを列挙し、それ以外は既定で拒否する |
| `src/backend/src/main/java/pro/kensait/leafbooks/security/CsrfTokenFilter.java` | 更新系の要求で Cookie とヘッダーのトークンを照合する |
| `src/frontend/src/pages/OrderConfirmPage.tsx`・`src/services/orderService.ts` | 非同期送信と失敗からの回復 |

| スクリプト | 確かめること |
|---|---|
| `curl/02_before_login.sh` | 書籍の一覧は 200、注文履歴と在庫はログイン前だと 401 |
| `curl/03_login.sh` | ログインで `Set-Cookie` が 2 つ（`leaf-books-jwt` と `XSRF-TOKEN`）届き、注文履歴が取れる |
| `curl/05_order_csrf.sh` | `X-XSRF-TOKEN` ヘッダーの無い注文は 403、付けると 200 |

## Codinable での変更点

講座本編は HSQLDB サーバーと Payara Server 6 を使います。Codinable では次のように変えて、単独で動くようにしてあります。

- データソース `jdbc/HsqldbDS` は、`payara/post-boot-commands.asadmin` で同梱の HSQLDB のインメモリ DB に登録します（サーバーの起動直後、デプロイの前に流れます）
- テーブルと初期データは、デプロイのたびに `persistence.xml` のスキーマ生成（`jakarta.persistence.schema-generation.*`）が `src/backend/sql/hsqldb/` の SQL から作ります。WAR に入れるときに `build.gradle` の `flattenSql` が 1 行 1 文の形に整えます
- 表紙画像（`src/backend/images/covers`）は WAR の `resources/images/covers` に入れ、`ImageResource` が配信します
- 仕様の API は Jakarta EE 11（Payara Server 7）でコンパイルします。`persistence.xml` などのスキーマには、元のサンプルの Jakarta EE 10 の版が残っています

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「JPQL・Criteria・楽観的ロックをAPIで動かす」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `src/backend/sql/hsqldb/2_LEAF_BOOKS_DDL.sql`、初期データ: `src/backend/sql/hsqldb/3_LEAF_BOOKS_DML.sql`）。

### PUBLISHER

| カラム | 型 | 制約 |
|---|---|---|
| `PUBLISHER_ID` | `INT` | 主キー、自動採番 |
| `PUBLISHER_NAME` | `VARCHAR(30)` | NOT NULL |

初期データ（5 件）

| PUBLISHER_ID | PUBLISHER_NAME |
|---|---|
| 1 | デジタルフロンティア出版 |
| 2 | コードブレイクプレス |
| 3 | ネットワークノード出版 |
| 4 | クラウドキャスティング社 |
| 5 | データドリフト社 |

### CATEGORY

| カラム | 型 | 制約 |
|---|---|---|
| `CATEGORY_ID` | `INT` | 主キー、自動採番 |
| `CATEGORY_NAME` | `VARCHAR(20)` | NOT NULL |

初期データ（9 件）

| CATEGORY_ID | CATEGORY_NAME |
|---|---|
| 1 | Java |
| 2 | SpringBoot |
| 3 | SQL |
| 4 | HTML/CSS |
| 5 | JavaScript |
| 6 | Python |
| 7 | 生成AI |
| 8 | クラウド |
| 9 | AWS |

### BOOK

| カラム | 型 | 制約 |
|---|---|---|
| `BOOK_ID` | `INT` | 主キー、自動採番 |
| `BOOK_NAME` | `VARCHAR(80)` | NOT NULL |
| `AUTHOR` | `VARCHAR(40)` | NOT NULL |
| `CATEGORY_ID` | `INT` | 外部キー → `CATEGORY.CATEGORY_ID`、NOT NULL |
| `PUBLISHER_ID` | `INT` | 外部キー → `PUBLISHER.PUBLISHER_ID`、NOT NULL |
| `PRICE` | `INT` | NOT NULL |
| `IMAGE_URL` | `VARCHAR(200)` |  |
| `DELETED` | `BOOLEAN` | NOT NULL、既定値 FALSE |

初期データ（50 件。先頭 12 件）

| BOOK_ID | BOOK_NAME | AUTHOR | CATEGORY_ID | PUBLISHER_ID | PRICE | IMAGE_URL | DELETED |
|---|---|---|---|---|---|---|---|
| 1 | Java SEディープダイブ | Michael Johnson | 1 | 3 | 3400 | /api/images/covers/1 | false |
| 2 | JVMとバイトコードの探求 | James Lopez | 1 | 1 | 4200 | /api/images/covers/2 | false |
| 3 | Javaアーキテクトのための設計原理 | David Jones | 1 | 4 | 3000 | /api/images/covers/3 | false |
| 4 | コンカレントプログラミング in Java SE | William Miller | 1 | 1 | 3500 | /api/images/covers/4 | false |
| 5 | Javaでのエレガントなコード設計 | Joseph Davis | 1 | 3 | 2800 | /api/images/covers/5 | false |
| 6 | Jakarta EE究極テストガイド | Thomas Rodriguez | 1 | 4 | 5200 | /api/images/covers/6 | false |
| 7 | Jakarta EEによるアーキテクチャ設計 | Chris Wilson | 1 | 3 | 3200 | /api/images/covers/7 | false |
| 8 | Jakarta EEパターンライブラリ | Daniel Hall | 1 | 1 | 4000 | /api/images/covers/8 | false |
| 9 | SpringBoot in Cloud | Paul Martin | 2 | 3 | 3000 | /api/images/covers/9 | false |
| 10 | SpringBootによるエンタープライズ開発 | Matthew Brown | 2 | 2 | 3900 | /api/images/covers/10 | false |
| 11 | SpringBoot魔法のレシピ | Tim Taylor | 2 | 4 | 4500 | /api/images/covers/11 | false |
| 12 | SpringBootアーキテクチャの深層 | Richard White | 2 | 1 | 2700 | /api/images/covers/12 | false |

### BOOK_IMAGE

| カラム | 型 | 制約 |
|---|---|---|
| `BOOK_ID` | `INT` | 主キー |
| `IMAGE_DATA` | `BLOB` | NOT NULL |
| `CONTENT_TYPE` | `VARCHAR(50)` | NOT NULL |

初期データはありません（演習のコードが登録します）。

### STOCK

| カラム | 型 | 制約 |
|---|---|---|
| `BOOK_ID` | `INT` | 主キー |
| `QUANTITY` | `INT` | NOT NULL |
| `VERSION` | `BIGINT` | NOT NULL |

初期データ（50 件。先頭 12 件）

| BOOK_ID | QUANTITY | VERSION |
|---|---|---|
| 1 | 3 | 0 |
| 2 | 2 | 0 |
| 3 | 1 | 0 |
| 4 | 3 | 0 |
| 5 | 2 | 0 |
| 6 | 2 | 0 |
| 7 | 0 | 0 |
| 8 | 3 | 0 |
| 9 | 2 | 0 |
| 10 | 3 | 0 |
| 11 | 2 | 0 |
| 12 | 1 | 0 |

### DEPARTMENT

| カラム | 型 | 制約 |
|---|---|---|
| `DEPARTMENT_ID` | `BIGINT` | 主キー、自動採番 |
| `DEPARTMENT_NAME` | `VARCHAR(100)` | NOT NULL |

初期データ（3 件）

| DEPARTMENT_ID | DEPARTMENT_NAME |
|---|---|
| 1 | 営業一部 |
| 2 | 営業二部 |
| 3 | 企画部 |

### EMPLOYEE

| カラム | 型 | 制約 |
|---|---|---|
| `EMPLOYEE_ID` | `BIGINT` | 主キー、自動採番 |
| `EMPLOYEE_CODE` | `VARCHAR(50)` | UNIQUE、NOT NULL |
| `EMPLOYEE_NAME` | `VARCHAR(100)` | NOT NULL |
| `EMAIL` | `VARCHAR(100)` |  |
| `PASSWORD` | `VARCHAR(100)` | NOT NULL |
| `DEPARTMENT_ID` | `BIGINT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID`、NOT NULL |
| `JOB_RANK` | `INT` | NOT NULL、既定値 1 |

初期データ（13 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_CODE | EMPLOYEE_NAME | EMAIL | PASSWORD | DEPARTMENT_ID | JOB_RANK |
|---|---|---|---|---|---|---|
| 1 | E00001 | 渡辺 誠 | watanabe@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 1 | 1 |
| 2 | E00002 | 中村 里奈 | nakamura@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 1 | 1 |
| 3 | E00003 | 小林 達也 | kobayashi@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 1 | 1 |
| 4 | E00004 | 坂本 徹 | sakamoto@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 1 | 2 |
| 5 | E00005 | 佐々木 良太 | sasaki@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 1 | 3 |
| 6 | E00006 | 加藤 恵 | kato@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 2 | 1 |
| 7 | E00007 | 吉田 麻美 | yoshida@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 2 | 1 |
| 8 | E00008 | 山本 修 | yamamoto@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 2 | 1 |
| 9 | E00009 | 井上 真理子 | inoue@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 2 | 2 |
| 10 | E00010 | 木村 真也 | kimura@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 2 | 3 |
| 11 | E00011 | 高橋 健太 | takahashi@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 3 | 1 |
| 12 | E00012 | 佐藤 花子 | sato@example.com | $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.… | 3 | 2 |

### ORDER_TRAN

| カラム | 型 | 制約 |
|---|---|---|
| `ORDER_TRAN_ID` | `INT` | 主キー、自動採番 |
| `ORDER_DATE` | `DATE` | NOT NULL |
| `CUSTOMER_ID` | `INT` | NOT NULL |
| `TOTAL_PRICE` | `INT` | NOT NULL |
| `DELIVERY_PRICE` | `INT` | NOT NULL |
| `DELIVERY_ADDRESS` | `VARCHAR(200)` | NOT NULL |
| `SETTLEMENT_TYPE` | `INT` | NOT NULL |

初期データ（4 件）

| ORDER_TRAN_ID | ORDER_DATE | CUSTOMER_ID | TOTAL_PRICE | DELIVERY_PRICE | DELIVERY_ADDRESS | SETTLEMENT_TYPE |
|---|---|---|---|---|---|---|
| 1 | 2023-03-01 | 1 | 5600 | 500 | 東京都中央区1-1-1 | 1 |
| 2 | 2023-04-01 | 1 | 5700 | 500 | 東京都中央区1-1-1 | 2 |
| 3 | 2023-05-01 | 1 | 11500 | 500 | 東京都中央区1-1-1 | 3 |
| 4 | 2023-06-01 | 1 | 4800 | 500 | 東京都中央区1-1-1 | 1 |

### ORDER_DETAIL

| カラム | 型 | 制約 |
|---|---|---|
| `ORDER_TRAN_ID` | `INT` | 主キー（複合）、外部キー → `ORDER_TRAN.ORDER_TRAN_ID` |
| `ORDER_DETAIL_ID` | `INT` | 主キー（複合） |
| `BOOK_ID` | `INT` | NOT NULL |
| `BOOK_NAME` | `VARCHAR(80)` | NOT NULL |
| `PUBLISHER_NAME` | `VARCHAR(80)` |  |
| `PRICE` | `INT` | NOT NULL |
| `COUNT` | `INT` | NOT NULL |

初期データ（7 件）

| ORDER_TRAN_ID | ORDER_DETAIL_ID | BOOK_ID | BOOK_NAME | PUBLISHER_NAME | PRICE | COUNT |
|---|---|---|---|---|---|---|
| 1 | 1 | 1 | Java SEディープダイブ | *(NULL)* | 3400 | 1 |
| 1 | 2 | 17 | SQLの冒険～RDBの深層 | *(NULL)* | 2200 | 1 |
| 2 | 1 | 12 | SpringBootアーキテクチャの深層 | *(NULL)* | 2700 | 1 |
| 2 | 2 | 24 | ES6＋完全ガイド | *(NULL)* | 3000 | 1 |
| 3 | 1 | 10 | SpringBootによるエンタープライズ開発 | *(NULL)* | 3900 | 1 |
| 3 | 2 | 26 | JSアーキテクチャパターンの探求 | *(NULL)* | 4200 | 1 |
| 3 | 3 | 33 | テスト自動化のためのPython | *(NULL)* | 3400 | 1 |
<!-- schema:end -->
