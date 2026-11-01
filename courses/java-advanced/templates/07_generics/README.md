# 07_generics — 総称型

## 何を学ぶ演習か

総称型がコンパイル時に何を保証するか、型消去、境界型パラメータ、ワイルドカードと PECS を確かめます（チャプター7.1）。

## 見るファイル（`src/main/java/pro/kensait/java/advanced/`）

- `lsn_7_1_2` … `interfaces`（原型の `StrLengthComparator1` と総称型の `StrLengthComparator2`）、`generictype`（`MyContainer`）
- `lsn_7_1_3` … 型パラメータの互換性（`Main_Invariant`）
- `lsn_7_1_4` … ジェネリックなメソッド（`genericmethod`・`extend`・`overload`）
- `lsn_7_1_5` … ワイルドカード（`unknown`・`capture`）
- `lsn_7_1_6` … PECS（`boundary_1`〜`boundary_3`）

## ボタンでの動かし方

- 動かしたいクラスをエディタで開き、右上の **ファイル実行** を押します。結果は「実行結果」に出ます。
- ツールバーの **アプリ実行** は、選んでいるレッスンの最初の main を動かします。
- 書き換えて動かなくなったら **↺ 初期化** で配布時の状態に戻せます。

## 結果のどこを見るか

- `interfaces.Main_2` は `ClassCastException` で終わります。原型のコンパレータに Integer のリストを渡した結果で、わざと例外を見せる教材です。
- コンパイルエラーになる行は、コメントにしてあります（`boundary_2.MyNumberUtil_1` の `list.add(100)` など）。コメントを外すと、エディタにエラーが出ます。
- `boundary_3.Main_2` は `List<Bar>` から `List<Foo>` へコピーします。
