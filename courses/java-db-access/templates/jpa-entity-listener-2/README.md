# jpa-entity-listener-2 — コールバックの責務と明示的な残高更新

講座リポジトリ learn_java_db_aidd の `projects/master/06_jpa-relations/jpa-entity-listener-2` を、Codinable で単独で動くようにしたものです。
`learn_java_db_access/jpa_entity_listener_2` から取り込んだ教材です。元のパッケージ・クラス名・例の流れを維持し、HSQLDB とビルドに合わせてあります。

## Codinable での動かし方

- ツールバーの **▶ アプリ実行** を押します。`mains.txt` に並べた main を上から順に実行し、
  出力は `===== [番号/件数] クラス名 =====` で区切られます。
- main ごとに `sql/hsqldb/*.sql`（テーブルの削除・作成・初期データ）を流し直すので、どの main も初期データから始まります。
  DB は HSQLDB のインメモリ（`jdbc:hsqldb:mem:testdb`）で、元の教材の HSQLDB サーバーや `setupHsqldb` は使いません。
- マッピングと各 main の実行はテストでも確かめられます。`JpaMappingTest.java`・`SampleMainTest.java`・`AccountBalanceServiceTest.java`（`src/test/java`）を開いて、エディタの右上の **▶ 実行** で流してください。
- main を 1 つだけ動かしたいときは、そのクラスを開いてエディタの右上の **▶ 実行** を押します（このときも先に初期データへ戻します）。
  `mains.txt` のほかの行の先頭に `#` を付けてから **▶ アプリ実行** を押しても同じです。
- 起動クラス `pro.kensait.course.SampleLauncher` と `mains.txt` は Codinable 用に足したものです。教材の main は元のままです。

## 読む順番

1. 下の「この演習で使うテーブル」（テーブル構成・初期データ。元は `sql/hsqldb/2_*.sql` と `3_*.sql`）を確認する。
2. `src/main/resources/META-INF/persistence.xml` とエンティティの注釈を確認する。
3. **▶ アプリ実行** で main を動かし、SQL・取得結果・更新後のデータを照合する。

## 実行する main（mains.txt の順）

- `pro.kensait.jpa.main.AccountBalanceMain`

## 取り込み時の補完

元教材は銀行口座のモデルに対してEmployee用のpersistence.xmlを持ち、初期データとmainがありませんでした。実エンティティの登録、最小データ、実行例を補っています。
元の `@PostLoad` は別のEntityManagerFactoryを生成して残高を書き換えていました。取得時の意図しない更新を避けるため、通知は `AccountListener`、未反映明細の加減算は `AccountBalanceService` に配置しました。これは責務の学習用の最小例で、同時更新を扱う実用の銀行システムではありません。

<!-- schema:begin -->
<!-- この節は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->
## この演習で使うテーブル

「読み込み通知と残高更新の責務の分離」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_TRAN_DDL.sql`、初期データ: `sql/hsqldb/3_TRAN_DML.sql`）。

### CIF

| カラム | 型 | 制約 |
|---|---|---|
| `CIF_NUM` | `INT` | 主キー |
| `CUSTOMER_NAME` | `VARCHAR(20)` | NOT NULL |

初期データ（1 件）

| CIF_NUM | CUSTOMER_NAME |
|---|---|
| 1 | Alice |

### ACCOUNT

| カラム | 型 | 制約 |
|---|---|---|
| `BRANCH_NUM` | `INT` | 主キー（複合） |
| `ACCOUNT_NUM` | `INT` | 主キー（複合） |
| `CIF_NUM` | `INT` | NOT NULL |
| `BALANCE` | `DECIMAL(13)` | NOT NULL |
| `LAST_TRAN_DATE` | `DATE` | NOT NULL |
| `LAST_TRAN_NUM` | `INT` | NOT NULL |

初期データ（1 件）

| BRANCH_NUM | ACCOUNT_NUM | CIF_NUM | BALANCE | LAST_TRAN_DATE | LAST_TRAN_NUM |
|---|---|---|---|---|---|
| 101 | 10001 | 1 | 200000 | 2026-01-01 | 0 |

### TRAN_DETAIL

| カラム | 型 | 制約 |
|---|---|---|
| `BRANCH_NUM` | `INT` | 主キー（複合） |
| `ACCOUNT_NUM` | `INT` | 主キー（複合） |
| `TRAN_DATE` | `DATE` | 主キー（複合） |
| `TRAN_NUM` | `INT` | 主キー（複合） |
| `PAY_REC_TYPE` | `VARCHAR(3)` | NOT NULL |
| `AMOUNT` | `DECIMAL(13)` | NOT NULL |

初期データ（2 件）

| BRANCH_NUM | ACCOUNT_NUM | TRAN_DATE | TRAN_NUM | PAY_REC_TYPE | AMOUNT |
|---|---|---|---|---|---|
| 101 | 10001 | 2026-01-01 | 1 | REC | 10000 |
| 101 | 10001 | 2026-01-02 | 1 | PAY | 5000 |
<!-- schema:end -->
