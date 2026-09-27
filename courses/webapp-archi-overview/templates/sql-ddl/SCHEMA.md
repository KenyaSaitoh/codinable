# DDL と制約で使うテーブル

この演習では、テーブルそのもの（入れ物）を作り・変え・消します。
使うのは商品を表す **PRODUCT** テーブルと、消すためだけに作る **SCRATCH** テーブルです。

## 動かし方

エディタ下の実行対象のセレクトから SQL ファイルを選び、「実行」を押します。
流した SQL がエディタに開き、結果やエラーは **SQL タブ**に出ます。

| 順番 | ファイル | 内容 |
|---|---|---|
| 1 | `01_create_table.sql` | PRODUCT を作り、3 件入れる（`reset.sql` と同じ内容） |
| 2 | `02_constraints.sql` | 制約に反する INSERT。**すべてエラーになる**ので 1 文ずつ選択して実行する |
| 3 | `03_alter_drop.sql` | カラムの追加・変更・削除（ALTER）と、テーブルの削除（DROP） |

- `02_constraints.sql` をまとめて実行すると最初のエラーで止まります。それも正しい動きです
- どのファイルも、「実行」するたびに `reset.sql` が先に流れて、テーブルが作り直され初期データに戻ります。何度実行しても同じ状態から始まります
- 範囲を選択して「実行」したときは作り直しません（1 文ずつ順に試せるように）

## PRODUCT（商品）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `PRODUCT_ID` | `INT` | 主キー | 商品番号 |
| `PRODUCT_NAME` | `VARCHAR(40)` | NOT NULL | 商品名 |
| `PRODUCT_CODE` | `CHAR(8)` | UNIQUE | 商品コード（重複を許さない・8 文字まで） |
| `PRICE` | `DECIMAL(10, 2)` | NOT NULL | 価格（小数 2 桁まで） |
| `STOCK` | `INT` | NOT NULL, 既定値 0 | 在庫数 |
| `RATING` | `INT` | CHECK（1〜5） | 評価 |
| `AVAILABLE` | `BOOLEAN` | NOT NULL, 既定値 TRUE | 販売中か |
| `RELEASE_DATE` | `DATE` | | 発売日 |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL, 既定値 現在時刻 | 登録日時 |

### 初期データ（`reset.sql`）

| PRODUCT_ID | PRODUCT_NAME | PRODUCT_CODE | PRICE | STOCK | RATING | RELEASE_DATE |
|---|---|---|---|---|---|---|
| 1 | ノート | NB-00001 | 480.00 | 120 | 4 | 2025-04-01 |
| 2 | 万年筆 | FP-00001 | 3200.50 | 8 | 5 | 2025-06-15 |
| 3 | インク | IK-00001 | 1100.00 | *(既定値 0)* | *(NULL)* | *(NULL)* |

3 件目は STOCK と AVAILABLE を省いているので、既定値（0 と TRUE）が入ります。

## `03_alter_drop.sql` で変わるところ

- PRODUCT に `CATEGORY VARCHAR(20)` を足し、`VARCHAR(40)` に広げ、`CATEGORY_NAME` に名前を変え、最後に削除する
- `SCRATCH`（`ID INT` 主キー、`MEMO VARCHAR(20)`）を作ってから、テーブルごと削除する
