# 16_string_regex — 文字列のAPI、正規表現

## 何を学ぶ演習か

`String` の不変性と文字列プール、API、`StringBuilder`、`Pattern` と `Matcher`、置換を確かめます。

## 見るファイルと動かし方

ツールバーの **▶ アプリ実行** で、演習一覧で選んだレッスンの main が動きます。ほかの main は、その `.java` を開いてエディタの右上の **▶ 実行** で動かします。実行結果は出力欄の「実行結果」に出ます。

| レッスン | 見るファイル | ▶ アプリ実行 | 結果のどこを見るか |
|:--|:--|:--|:--|
| 16.1.1 Stringの不変性と文字列プール | `…/lsn_16_1_1/Main.java`<br>`…/lsn_16_1_1/Main_Reference.java` | `lsn_16_1_1.Main` | snippet_3 の true と snippet_4 の false |
| 16.1.2 StringのAPIの仕組み | `…/lsn_16_1_2/Main_Conversion.java`<br>`…/lsn_16_1_2/Main_Substring.java`<br>`…/lsn_16_1_3/Main.java` | `lsn_16_1_2.Main_Conversion` | snippet_1 の `Foo_Bar_Foobar` と snippet_2 の `FOO_BAR_FOOBAR` |
| 16.1.3 文字列連結の仕組みとStringBuilder | `…/lsn_16_1_4/Main.java` | `lsn_16_1_4.Main` | snippet_1 と snippet_2 の完了までの時間の差 |
| 16.2.2 PatternとMatcherの仕組み | `…/lsn_16_2_3/Main_Matches_1.java`<br>`…/lsn_16_2_3/Main_Find.java` | `lsn_16_2_3.Main_Matches_1` | snippet_1・snippet_2 の matches、lookingAt、group |
| 16.2.3 グループと置換・形式チェックの落とし穴 | `…/lsn_16_2_3/Main_FormatCheck.java`<br>`…/lsn_16_2_4/Main_Replace_2.java` | `lsn_16_2_4.Main_Replace_2` | snippet_2、snippet_5〜snippet_7 の置換の結果 |

`…/` は `src/main/java/pro/kensait/java/basic/` の略です。
