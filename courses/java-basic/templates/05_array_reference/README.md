# 05_array_reference — 配列、参照型変数とnull値

## 何を学ぶ演習か

配列の構造と範囲、多次元配列、参照の共有を確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 5.1.1 配列の構造 | `src/main/java/Main_Average_2.java`<br>`src/main/java/Main_5_1.java` | `Main_Average_2` | 平均スコアの 640.0。`Main_5_1` の snippet_8 では `Index 3 out of bounds for length 3` |
| 5.1.2 多次元配列 | `src/main/java/Main_5_1.java` | `Main_5_1` | snippet_8 のスタックトレースと、それに続く snippet_9〜snippet_12 |
| 5.2.1 参照の代入と共有 | `src/main/java/Main_5_2.java` | `Main_5_2` | snippet_2 の 660 |
| 6.3.1 for文と拡張for文 | `src/main/java/Main_Average_2.java` | `Main_Average_2` | 平均スコアの 640.0 |

## 注意

* `Main_5_1` の snippet_8 は、後ろの snippet も動くように例外を捕まえて `printStackTrace()` で表示します。
