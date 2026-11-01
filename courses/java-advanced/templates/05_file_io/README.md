# 05_file_io — ファイル入出力とシリアライズ

## 何を学ぶ演習か

`Path` と `Files`、ストリームの層、文字コード、リソースのクローズ、バイト配列ストリーム、シリアライズを確かめます（チャプター5.1・5.2）。

## 見るファイル

`src/main/java/pro/kensait/java/advanced/` の下です。

- `lsn_5_1_1` … `Path` と `Files`
- `lsn_5_1_3` … `Main_BufferedReader`（UTF-8 と Shift_JIS の読み込み）、`Main_BufferedWriter`
- `lsn_5_1_4` … `Main_BufferedInputStream`、`Main_BufferedOutputStream`（画像のコピー）
- `lsn_5_2_1` … `ByteArrayOutputStream` と `ByteArrayInputStream`
- `lsn_5_2_2` … `Person`（シリアライズするクラス）、`Main_Ser_1`（書く）、`Main_Ser_2`（読む）

読み書きするファイルはプロジェクト直下にあります。

- `hoge/fuga/foo.txt`（UTF-8）、`hoge/fuga/bar.txt`（Shift_JIS）、`java_logo1.jpg`

## ボタンでの動かし方

- 動かしたいクラスをエディタで開き、右上の **▶ 実行** を押します。結果は「実行結果」に出ます。
- ツールバーの **▶ アプリ実行** は、選んでいるレッスンの最初の main を動かします。
- 書き換えて動かなくなったら **↺ 初期化** で配布時の状態に戻せます。

## 結果のどこを見るか

- `Main_BufferedReader` は foo.txt と bar.txt の 3 行ずつを表示します。文字化けしないのは、それぞれの文字コードを指定しているからです。
- `Main_BufferedOutputStream` と `lsn_5_2_1.Main` は画面に何も出しません。ツリーの `java_logo2.jpg` を見ます。
- `Main_BufferedWriter` は `hoge/fuga/foo2.txt` を作ります。
- `Main_Ser_1` がプロジェクト直下に `alice.ser` を書き、続けて `Main_Ser_2` を動かすと `Person [name=Alice, age=25, gender=female]` を表示します。
