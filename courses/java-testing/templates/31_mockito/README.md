# 31_mockito: モックとスパイ

## 何を学ぶ演習か

Mockito でモックとスパイを作り、振る舞いの設定と呼び出しの検証を確かめます（チャプター3.1）。チャプター1.4 の検証パターンの題材もここにあります。

## 見るファイル

- `src/test/java/pro/kensait/mockito/mock/behavior/BasicMockTest_1.java`: when-then と do-when
- `src/test/java/pro/kensait/mockito/calc/normal/CalcTest_ArgMatchers.java`: 引数マッチング
- `src/test/java/pro/kensait/mockito/calc/normal/CalcTest_DynamicAnswer.java`: `Answer`
- `src/test/java/pro/kensait/mockito/mock/verify/VerifyTest.java`: `verify()`
- `src/test/java/pro/kensait/mockito/spy/DoNothingTest.java`: スパイと `doNothing()`
- `build.gradle`: `mockito-core` を `-javaagent` で渡す設定

## ボタンでの動かし方

テストクラスを開いて、エディタの右上の ファイル実行を押します。

## 結果のどこを見るか

- 「テスト結果」のメソッドごとの成否
- 実行結果に出る、モックから返った値
- 失敗したときの `WantedButNotInvoked` や `UnnecessaryStubbingException` のメッセージ
