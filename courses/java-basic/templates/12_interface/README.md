# 12_interface — インタフェースと多態性

## 何を学ぶ演習か

インタフェースによる外部仕様の分離、クラスの階層と無関係な機能の追加、default メソッドを確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 12.1.1 インタフェースによる外部仕様の分離 | `…/lsn_12_1_1/Piyo.java` | `lsn_12_1_1.Main` | `Hoge says YES!` と `Piyo says YES!` |
| 12.1.2 クラスの階層と無関係な機能の追加 | `…/lsn_12_1_2/NameAggregator.java`<br>`…/lsn_12_1_2/NameAggregators.java` | `lsn_12_1_2.Main` | 名寄せ先の point=4000 と名寄せ元の invalid=true |
| 12.1.3 インタフェースの様々なメンバーと抽象クラスとの使い分け | `…/lsn_12_1_2/FamilySpec.java` | `lsn_12_1_2.Main` | 名寄せ先の point=4000。`FamilySpec` の default メソッドも読みます |

`…/` は `src/main/java/pro/kensait/java/basic/` の略です。
