# 13_object — Objectクラスとオブジェクトの特性

## 何を学ぶ演習か

`toString()`、`equals()` と `hashCode()`、イミュータブル、シャローコピーとディープコピーを確かめます。

## 見るファイルと動かし方

ツールバーの **アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **ファイル実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 13.1.1 Objectクラスの役割 | `…/lsn_13_1_1/Person.java` | `lsn_13_1_1.Main` | `Person [name=Alice, age=25, address=中央区1-1-1]` |
| 13.1.2 同一性と等価性、equals()とhashCode()の契約 | `…/lsn_13_1_2/Person.java` | `lsn_13_1_2.Main` | `false, true`（`==` で別のインスタンスと同じインスタンスを比べた結果） |
| 13.1.3 参照の値渡しとイミュータブル | `…/lsn_13_1_3/mutable/Main_2.java`<br>`…/lsn_13_1_3/immutable/Person.java` | `lsn_13_1_3.mutable.Main_2` | 住所が 中央区2-2-2 に変わっていること |
| 13.1.4 インスタンスのコピー | `…/lsn_13_1_4/copy/Main_ShallowCopy.java`<br>`…/lsn_13_1_4/clone/Person.java` | `lsn_13_1_4.copy.Main_ShallowCopy` | コピー先の addressLine=2-2-2 |

`…/` は `src/main/java/pro/kensait/java/basic/` の略です。
