# 03_var_type_literal — 変数とデータ型、リテラル、型変換、型推論

## 何を学ぶ演習か

プリミティブ型と参照型の違い、リテラルの書き方、縮小変換で失われる値、`var` の推論を、複数のコード例（snippet）で確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 3.1.1 変数と型 | `src/main/java/Main_3_1.java` | `Main_3_1` | snippet_7 の 30 |
| 3.1.2 プリミティブ型と参照型 | `src/main/java/Main_3_3.java` | `Main_3_3` | snippet_3 の 10 と snippet_4 の foo |
| 3.1.3 プリミティブ型の範囲と表現の限界 | `src/main/java/Main_3_1.java` | `Main_3_1` | snippet_8・snippet_9 のコード（標準では値を表示しません。AI活用で println を足して確かめます） |
| 3.2.1 数値リテラルと既定の型 | `src/main/java/Main_3_3.java`<br>`src/main/java/Main_3_2.java` | `Main_3_2` | snippet の見出しの並び（数値はコードのコメントと比べます） |
| 3.2.2 文字・文字列リテラルとテキストブロック | `src/main/java/Main_3_2.java`<br>`src/main/java/Main_TextBlock.java` | `Main_TextBlock` | テキストブロックの各行のインデント。`Main_3_2` では snippet_7 のタブと snippet_8 の `'foo'` |
| 3.3.1 型チェックと型変換 | `src/main/java/Main_3_3.java` | `Main_3_3` | snippet_5〜snippet_9 のコード。コンパイルエラーになる行は `//` で無効にしてあります |
| 3.3.2 縮小変換で失われるもの | `src/main/java/Main_3_3.java` | `Main_3_3` | snippet_10・snippet_11 のコード（AI活用で println を足すと -112 と 12345） |
| 3.3.3 varによる型推論 | `src/main/java/Main_3_3.java` | `Main_3_3` | snippet_16〜snippet_18 の `var` のコード |

## 注意

* snippet の多くは値を表示しません。「このチャプターにおけるAI活用」で `println` を足して確かめます。
* コンパイルエラーを示す行は、先頭の `//` で無効にしてあります。
