# 07_class_instance — クラスとインスタンス、メソッド、オーバーロード

## 何を学ぶ演習か

クラスの宣言、フィールドとメソッド、コンストラクタ、オーバーロード、メソッドチェーン、再帰、状態の有無を確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 7.1.2 クラスの宣言とインスタンスの生成 | `src/main/java/Person.java` | `Main_7_2` | snippet_2 の name と age |
| 7.2.1 フィールドとメソッド | `src/main/java/Person2.java`<br>`src/main/java/Main_7_2.java`<br>`src/main/java/Printer.java`<br>`src/main/java/Calculator.java`<br>`src/main/java/Main_Calculator.java` | `Main_Calculator` | 足し算の結果の 40 |
| 7.2.2 スコープとthis | `src/main/java/Person3.java` | `Main_7_2` | `Person3` のメソッドと、snippet_3・snippet_4 の挨拶の行 |
| 7.2.3 コンストラクタ | `src/main/java/Main_7_2.java` | `Main_7_2` | snippet_4 の Alice と Bob の行 |
| 7.3.1 シグネチャとオーバーロード | `src/main/java/Person3.java` | `Main_MethodChain` | snippet_1 のメッセージ |
| 7.3.2 メソッド呼び出しの形と処理の流れ | `src/main/java/Person3.java`<br>`src/main/java/Main_Recursive.java` | `Main_Recursive` | 再帰の合計の 55 |
| 7.3.3 クラスの状態の有無 | `src/main/java/StatefulCalc.java` | `Main_StatefulCalc` | 足し算の結果の 40 |
