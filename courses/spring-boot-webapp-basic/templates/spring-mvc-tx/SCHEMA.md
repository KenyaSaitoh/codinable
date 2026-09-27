# この演習で使うテーブル

「REQUIRED と REQUIRES_NEW の伝播」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/1_SPRING_TX_DDL.sql`、初期データ: `sql/hsqldb/2_SPRING_TX_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## BUSINESS

| カラム | 型 | 制約 |
|---|---|---|
| `NAME` | `VARCHAR(10)` | 主キー |
| `COUNT` | `INT` | NOT NULL |

初期データ（2 件）

| NAME | COUNT |
|---|---|
| Bar | 0 |
| Qux | 0 |
