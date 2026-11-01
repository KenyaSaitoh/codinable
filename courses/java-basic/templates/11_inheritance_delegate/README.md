# 11_inheritance_delegate — 継承と多態性、委譲

## 何を学ぶ演習か

コンストラクタの連鎖、オーバーライド、抽象クラス、キャストと多態性、委譲を確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 11.1.1 継承の構造とコンストラクタの連鎖 | `…/lsn_11_1_1/case_2/Bar.java` | `lsn_11_1_1.case_2.Main` | `subtract()` の結果の 60 |
| 11.1.2 オーバーライドの成立条件 | `…/lsn_11_1_2/case_2/Bar.java`<br>`…/lsn_11_1_2/case_3/Bar.java` | `lsn_11_1_2.case_2.Main` | `add()` の結果の 140。`case_3.Main` では 140 と 60 |
| 11.1.3 抽象クラスによる共通化と実装の強制 | `…/lsn_11_1_3/CustomerBase.java`<br>`…/lsn_11_1_3/GeneralCustomer.java` | `lsn_11_1_3.Main` | snippet_1 の 5000 と snippet_2 の 20000 |
| 11.1.4 型の変換と多態性 | `…/lsn_11_1_4/Main_4.java`<br>`…/lsn_11_1_5/Transaction.java` | `lsn_11_1_4.Main_4` | OK が 2 行。`lsn_11_1_5.Main_2` では 5000 と 20000 |
| 11.2.1 委譲の構造 | `…/lsn_11_2_1/GeneralCustomer.java` | `lsn_11_1_3.Main` | `lsn_11_2_1.GeneralCustomer` の委譲のコード（main がないので、ボタンは比較元の `lsn_11_1_3.Main` を動かします） |

`…/` は `src/main/java/pro/kensait/java/basic/` の略です。
