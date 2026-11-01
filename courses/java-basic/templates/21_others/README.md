# 21_others — 入れ子クラス、Optional、その他のクラスとツール

## 何を学ぶ演習か

入れ子クラス、`Optional`、`System`、プロパティファイル、国際化、`java` コマンドと Javadoc を確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 21.1.2 メンバークラスによる内部構造の隠蔽 | `…/lsn_21_1_3/ScoreManager.java` | `lsn_21_1_3.Main_2` | 90、85、70、90 |
| 21.1.3 匿名クラスとクロージャ | `…/lsn_21_1_5/Main_3.java` | `lsn_21_1_5.Main_3` | 100 + 30 + 10 の 140 |
| 21.2.1 nullの入り込む経路とnullチェック | `…/lsn_21_2_1/Main.java` | `lsn_21_2_1.Main` | `nullCheck1` の true と、続く `nullCheck2` の `NullPointerException`（わざと例外で終わります） |
| 21.2.2 Optionalによる戻り値と引数の設計 | `…/lsn_21_2_2/PersonDBSearcher.java` | `lsn_21_2_2.Main` | snippet_1 の empty と snippet_2 の既定の値 |
| 21.3.1 Systemクラスと実行環境 | `…/lsn_21_3_1/Main.java` | `lsn_21_3_1.Main` | snippet_1 の WHITE と snippet_2 のホームディレクトリ |
| 21.3.2 プロパティファイルによる値の外部化 | `src/MyResource.properties`<br>`…/lsn_21_3_2/Main.java` | `lsn_21_3_2.Main` | `アリス, 25, 中央区 1-1-1` が 2 回 |
| 21.3.3 国際化対応 | `…/lsn_21_3_3/Main.java` | `lsn_21_3_3.Main` | snippet_5 の ￥10,000、$10,000.00、£10,000.00 |
| 21.4.1 javaコマンドとプログラムへの値の渡し方 | `…/lsn_21_4_1/Hello.java` | `lsn_21_4_1.Main_JVM_Param` | `program.mode => null`。`lsn_21_4_1.Hello` は引数なしだと `ArrayIndexOutOfBoundsException` です |
| 21.4.3 JavadocによるAPIの約束事の記述 | `…/lsn_21_4_3/JavadocSample.java` | `lsn_21_4_1.Main_JVM_Param` | `lsn_21_4_3.JavadocSample` のコメントとタグ（main がないので、ボタンは `Main_JVM_Param` を動かします） |

`…/` は `src/main/java/pro/kensait/java/basic/` の略です。

## 注意

* `src/MyResource.properties` と `src/MyResource_en_US.properties` はクラスパスに入ります（`build.gradle`）。`src` の直下に足した `.properties` も読めます。
* `lsn_21_3_1.Main_SystemIn` はキーボードの入力を読むクラスです。Codinable では入力の代わりに `stdin/pro.kensait.java.basic.lsn_21_3_1.Main_SystemIn.txt` の行を渡します。
* `lsn_21_4_1.Hello` はコマンドライン引数を読みます。▶ 実行は引数を渡さないので、引数を渡すときはスクリプト（`.sh`）を作ります。ソースのパスは `src/main/java/pro/kensait/...` です。
* Javadoc のコマンドで `-sourcepath` に指定するのも `src/main/java` です。
