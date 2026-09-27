# この演習で使うテーブル

「JPAの関連・検索・楽観的ロックを動かす」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_BOOKSTORE_DDL.sql`、初期データ: `sql/hsqldb/3_BOOKSTORE_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## PUBLISHER

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

## CATEGORY

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

## BOOK

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

## BOOK_IMAGE

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `BOOK_ID` | `INT` | 主キー | 書籍ID（0はフォールバック画像） |
| `IMAGE_DATA` | `BLOB` | NOT NULL | 画像バイナリ |
| `CONTENT_TYPE` | `VARCHAR(50)` | NOT NULL | MIMEタイプ |

初期データはありません（演習のコードが登録します）。

## STOCK

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

## ORDER_TRAN

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

## ORDER_DETAIL

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
