# Leaf Books（MVC 版）

Spring MVC + Thymeleaf でサーバーが HTML を生成する書店アプリです（ポート 8080）。
書籍の検索、カート、ログイン、注文、在庫更新、注文履歴までを扱います。
ログインと顧客登録では、外部 API の Customer Hub（ポート 8081）を呼びます。

## この雛形を使う演習

| 演習 | 主に読むファイル |
|---|---|
| MVC版のServiceとDTOを読む（2.1） | `service/order/OrderService.java`（orderBooks）、`service/order/OrderTO.java`、`service/delivery/DeliveryFeeService.java` |
| JPAの関連・検索・楽観的ロックを動かす（3.1） | `entity/OrderDetail.java`・`OrderDetailPK.java`、`repository/BookRepository.java`、`service/book/BookService.java`、`repository/StockRepository.java`、テーブルは 下の「この演習で使うテーブル」 |
| 外部APIの呼び出しと入力検証を試す（3.1） | `external/CustomerApiClient.java`、`web/customer/CustomerParam.java` |
| MVC版の画面遷移・セッション・PRGを動かす（4.1） | `web/order/OrderController.java`、`web/cart/CartSession.java`、`web/login/TokenProcessor.java` |

パスは `src/main/java/pro/kensait/leafbooks/` からの相対です。

## Codinable での動かし方

1. ツールバーの **▶ アプリ実行** を押す
   → 書店が `http://localhost:8080` で起動し、プレビュータブが開く。
   ログイン・顧客登録で呼ぶ Customer Hub（`customer-hub/`、ポート 8081）も一緒に起動する
   （`codinable.services.json` に書いてある。出力には `[customer-hub]` を付けて出る）

2. ログイン画面でメールアドレス `alice@gmail.com`、パスワード `password` を入れる
   （bob / carol / dave / ellen も同じパスワード）

3. 書籍選択 → カート → 注文 → 注文履歴と進める。注文完了画面で再読み込みしても、
   Post/Redirect/Get なので注文は二重になりません

Customer Hub は「停止」では止まりません。出力タブの行の右端のボタンで止めたり起動し直したりできます。
止めた状態でログインすると、接続エラーになるのが確かめられます。
書籍の一覧や検索はログイン後の画面から使えます。

## テスト

`src/test/java/pro/kensait/leafbooks/` のテストクラスを開いて、エディタの右上の ▶ 実行で流してください。
Controller（MockMvc。例: `web/order/OrderControllerTest.java`）・Service（Mockito。例: `service/order/OrderServiceTest.java`）・
Repository（`@DataJpaTest`。例: `repository/StockRepositoryTest.java`）のテストがあります。
JPA の演習では `StockRepositoryTest.java` の「バージョンが一致しない場合、更新が失敗すること」を見ます。

## DB

HSQLDB のインメモリで動かしています。起動のたびに `sql/hsqldb/` の
`2_BOOKSTORE_DDL.sql` と `3_BOOKSTORE_DML.sql` から作り直されるので、
▶ アプリ実行のたびに初期データから始まります（注文や在庫の変更は残りません）。
Customer Hub も ▶ アプリ実行のたびに起動し直すので、登録した顧客も初期データに戻ります。
元の講座のように HSQLDB サーバーを起動する必要はありません。
テーブル構成と初期データは下の「この演習で使うテーブル」にまとめてあります。

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

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「JPAの関連・検索・楽観的ロックを動かす」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_BOOKSTORE_DDL.sql`、初期データ: `sql/hsqldb/3_BOOKSTORE_DML.sql`）。

### PUBLISHER

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `PUBLISHER_ID` | `INT` | 主キー、自動採番 | 出版社ID |
| `PUBLISHER_NAME` | `VARCHAR(30)` | NOT NULL | 出版社名 |

初期データ（5 件）

| PUBLISHER_ID | PUBLISHER_NAME |
|---|---|
| 1 | デジタルフロンティア出版 |
| 2 | コードブレイクプレス |
| 3 | ネットワークノード出版 |
| 4 | クラウドキャスティング社 |
| 5 | データドリフト社 |

### CATEGORY

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `CATEGORY_ID` | `INT` | 主キー、自動採番 | カテゴリID |
| `CATEGORY_NAME` | `VARCHAR(20)` | NOT NULL | カテゴリ名 |

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

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `BOOK_ID` | `INT` | 主キー、自動採番 | 書籍ID |
| `BOOK_NAME` | `VARCHAR(80)` | NOT NULL | 書籍名 |
| `AUTHOR` | `VARCHAR(40)` | NOT NULL | 著者 |
| `CATEGORY_ID` | `INT` | 外部キー → `CATEGORY.CATEGORY_ID`、NOT NULL | カテゴリID |
| `PUBLISHER_ID` | `INT` | 外部キー → `PUBLISHER.PUBLISHER_ID`、NOT NULL | 出版社ID |
| `PRICE` | `INT` | NOT NULL | 価格 |

初期データ（50 件。先頭 12 件）

| BOOK_ID | BOOK_NAME | AUTHOR | CATEGORY_ID | PUBLISHER_ID | PRICE |
|---|---|---|---|---|---|
| 1 | Java SEディープダイブ | Michael Johnson | 1 | 3 | 3400 |
| 2 | JVMとバイトコードの探求 | James Lopez | 1 | 1 | 4200 |
| 3 | Javaアーキテクトのための設計原理 | David Jones | 1 | 4 | 3000 |
| 4 | コンカレントプログラミング in Java SE | William Miller | 1 | 1 | 3500 |
| 5 | Javaでのエレガントなコード設計 | Joseph Davis | 1 | 3 | 2800 |
| 6 | Jakarta EE究極テストガイド | Thomas Rodriguez | 1 | 4 | 5200 |
| 7 | Jakarta EEによるアーキテクチャ設計 | Chris Wilson | 1 | 3 | 3200 |
| 8 | Jakarta EEパターンライブラリ | Daniel Hall | 1 | 1 | 4000 |
| 9 | SpringBoot in Cloud | Paul Martin | 2 | 3 | 3000 |
| 10 | SpringBootによるエンタープライズ開発 | Matthew Brown | 2 | 2 | 3900 |
| 11 | SpringBoot魔法のレシピ | Tim Taylor | 2 | 4 | 4500 |
| 12 | SpringBootアーキテクチャの深層 | Richard White | 2 | 1 | 2700 |

### BOOK_IMAGE

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `BOOK_ID` | `INT` | 主キー | 書籍ID（0はフォールバック画像） |
| `IMAGE_DATA` | `BLOB` | NOT NULL | 画像バイナリ |
| `CONTENT_TYPE` | `VARCHAR(50)` | NOT NULL | MIMEタイプ |

初期データはありません（演習のコードが登録します）。

### STOCK

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `BOOK_ID` | `INT` | 主キー | 書籍ID |
| `QUANTITY` | `INT` | NOT NULL | 在庫数 |
| `VERSION` | `BIGINT` | NOT NULL | バージョン番号 |

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

### ORDER_TRAN

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `ORDER_TRAN_ID` | `INT` | 主キー、自動採番 | 注文取引ID |
| `ORDER_DATE` | `DATE` | NOT NULL | 注文日 |
| `CUSTOMER_ID` | `INT` | NOT NULL | 顧客ID |
| `TOTAL_PRICE` | `INT` | NOT NULL | 注文金額合計 |
| `DELIVERY_PRICE` | `INT` | NOT NULL | 配送料金 |
| `DELIVERY_ADDRESS` | `VARCHAR(30)` | NOT NULL | 配送先住所 |
| `SETTLEMENT_TYPE` | `INT` | NOT NULL | 決済方法 |

初期データ（4 件）

| ORDER_TRAN_ID | ORDER_DATE | CUSTOMER_ID | TOTAL_PRICE | DELIVERY_PRICE | DELIVERY_ADDRESS | SETTLEMENT_TYPE |
|---|---|---|---|---|---|---|
| 1 | 2023-03-01 | 1 | 5600 | 500 | 東京都中央区1-1-1 | 1 |
| 2 | 2023-04-01 | 1 | 5700 | 500 | 東京都中央区1-1-1 | 2 |
| 3 | 2023-05-01 | 1 | 11500 | 500 | 東京都中央区1-1-1 | 3 |
| 4 | 2023-06-01 | 1 | 4800 | 500 | 東京都中央区1-1-1 | 1 |

### ORDER_DETAIL

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `ORDER_TRAN_ID` | `INT` | 主キー（複合）、外部キー → `ORDER_TRAN.ORDER_TRAN_ID` | 注文取引ID |
| `ORDER_DETAIL_ID` | `INT` | 主キー（複合） | 注文明細ID |
| `BOOK_ID` | `INT` | 外部キー → `BOOK.BOOK_ID`、NOT NULL | 書籍ID |
| `PRICE` | `INT` | NOT NULL | 価格 |
| `COUNT` | `INT` | NOT NULL | 注文数 |

初期データ（7 件）

| ORDER_TRAN_ID | ORDER_DETAIL_ID | BOOK_ID | PRICE | COUNT |
|---|---|---|---|---|
| 1 | 1 | 1 | 3400 | 1 |
| 1 | 2 | 17 | 2200 | 1 |
| 2 | 1 | 12 | 2700 | 1 |
| 2 | 2 | 24 | 3000 | 1 |
| 3 | 1 | 10 | 3900 | 1 |
| 3 | 2 | 26 | 4200 | 1 |
| 3 | 3 | 33 | 3400 | 1 |
<!-- schema:end -->
