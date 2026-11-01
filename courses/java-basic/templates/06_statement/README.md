# 06_statement — 条件分岐、繰り返し

## 何を学ぶ演習か

if文の条件の順序、switch、for・while、ループのネストを確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 6.2.1 if文の構造と条件の順序 | `src/main/java/Main_If_1.java`<br>`src/main/java/Main_If_2.java`<br>`src/main/java/Main_If_3.java` | `Main_6_2` | `Main_6_2` が動くこと。`Main_If_1`〜`Main_If_3` は入力の行を固定値に書き換えてから ▶ 実行します |
| 6.2.2 複数の条件の組み合わせ | `src/main/java/Main_If_7.java`<br>`src/main/java/Main_If_8.java` | `Main_6_2` | `Main_6_2` が動くこと。`Main_If_7`・`Main_If_8` も入力の行を固定値に書き換えて比べます |
| 6.2.3 switch文とフォールスルー | `src/main/java/Main_Switch_1.java` | `Main_6_2` | `Main_6_2` が動くこと。`Main_Switch_1` は入力の行を固定値に書き換えて配送料を確かめます |
| 6.2.4 switch式と網羅性 | `src/main/java/Main_Switch_4.java` | `Main_6_2` | `Main_6_2` が動くこと。`Main_Switch_4` は入力の行を固定値に書き換えて配送料を確かめます |
| 6.2.5 三項演算子と分岐の書き方の選択 | `src/main/java/Main_TO.java` | `Main_6_2` | `Main_6_2` が動くこと。`Main_TO` は入力の行を固定値に書き換えて配送料を確かめます |
| 6.3.1 for文と拡張for文 | `src/main/java/Main_6_3.java`<br>`src/main/java/Main_Average_3.java` | `Main_6_3` | snippet_1 のカウンタ変数の行。`Main_Average_3` では 640.0 |
| 6.3.2 while文とdo-while文 | `src/main/java/Main_6_3.java` | `Main_6_3` | snippet_5 と snippet_7 の 15625 |
| 6.3.3 ループの制御と無限ループ | `src/main/java/Main_6_3.java` | `Main_6_3` | snippet_10 までが最後まで流れること |
| 6.3.4 ループのネスト | `src/main/java/Main_NestedLoop_1.java`<br>`src/main/java/Main_NestedLoop_3.java` | `Main_NestedLoop_1` | Dave の 93.75。`Main_NestedLoop_3` では「追試開催」が 1 回だけ |

## 注意

* `Main_If_*`・`Main_Switch_*`・`Main_TO` はキーボードの入力を読むクラスです。Codinable では入力の代わりに `stdin/<クラス名>.txt` の行を順に渡すので、開いて右上の **▶ 実行** でそのまま動きます。`stdin/` のファイルの値を書き換えて流し直すと、境界の前後の動きを確かめられます。
* 無限ループになったら、出力欄の「停止」で止めます。
