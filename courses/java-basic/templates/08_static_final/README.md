# 08_static_final — スタティックなメンバー、finalと定数

## 何を学ぶ演習か

インスタンスで共有されるスタティックフィールド、スタティックメソッド、`final` と定数を確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 8.1.2 スタティックフィールドと共有される状態 | `src/main/java/Person.java` | `Main_Person` | snippet_2 の name と age |
| 8.1.3 スタティックメソッドとスタティック初期化子 | `src/main/java/StaticCalculator.java` | `Main_StaticCalculator` | 足し算の結果の 40 |
| 8.2.1 finalによる再代入の禁止 | `src/main/java/Person2.java` | `Main_NewPage` | 30 行ごとの「改ページ」の位置 |
| 8.2.2 定数 | `src/main/java/Customer.java` | `Main_NewPage` | 30 行ごとの「改ページ」の位置（定数 `Customer` のコードも読みます） |
