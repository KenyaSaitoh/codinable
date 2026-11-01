# 08_reflection_annotation — クラスローダー・リフレクション・アノテーション

## 何を学ぶ演習か

クラスのロードと初期化の時点、クラスローダーの階層、リフレクションによる生成と呼び出し、アノテーションの読み取りを確かめます（チャプター8.1〜8.3）。

## 見るファイル（`src/main/java/pro/kensait/java/advanced/`）

- `lsn_8_1_2` … `Greeting` と `Main`（初期化の時点）
- `lsn_8_1_1` … `Main_1`（クラスローダーの階層）、`Main_2`（`src/main/resources/config/MyResource.properties` の読み込み）
- `lsn_8_2_2` … `Main_MethodInvocation_1`〜`3`、`Main_Constructor`、`Main_MetaInfo`、`Main_InstanceCopy`（`Person` から `Customer` への詰め替え）
- `lsn_8_3_2` … `javase`（Java SE のアノテーション）、`metainfo`（`MyMetaInfo`）、`maxlength`（`@MaxLength` と `AnnotationProcessor`）

## ボタンでの動かし方

- 動かしたいクラスをエディタで開き、右上の **▶ 実行** を押します。結果は「実行結果」に出ます。
- ツールバーの **▶ アプリ実行** は、選んでいるレッスンの最初の main を動かします。
- 書き換えて動かなくなったら **↺ 初期化** で配布時の状態に戻せます。

## 結果のどこを見るか

- `lsn_8_1_2.Main` は、snippet_1 では時刻が出ず、snippet_2 で初めて出ます。
- `lsn_8_1_1.Main_1` は、1 行目と 4 行目の `@` の後ろの値が一致します（値は実行のたびに変わります）。
- `Main_InstanceCopy` は `Customer [id=1, customerName=Alice, gender=FEMALE, address=中央区1-1-1]` と表示します。
- `metainfo.Main_Custom_3` の引数の名前は `arg0` です。
- `maxlength.Main` は「文字列長が指定された長さを超えています」の例外で終わります。11 文字の値を検証した結果で、わざと例外を見せる教材です。
