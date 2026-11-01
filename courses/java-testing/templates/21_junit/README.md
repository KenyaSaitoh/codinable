# 21_junit: JUnit による単体テスト

## 何を学ぶ演習か

JUnit 6 のテストメソッドの構造、アサーション、ライフサイクル、ネステッドクラス、パラメータ化テスト、例外のテストを、動かして確かめます（チャプター2.1）。Gradle によるグループ分け（チャプター2.3）と、チャプター1.3・1.4・10.1 の題材もここにあります。

## 見るファイル

- `src/test/java/pro/kensait/junit/calc/normal/CalculatorTest_1.java`、`CalculatorTest_2.java`: テストの基本形
- `src/test/java/pro/kensait/junit/assertion1/AssertEqualsTest.java`: アサーションと許容誤差
- `src/test/java/pro/kensait/junit/lifecycle/NestedLifeCycleTest.java`: ライフサイクルの呼び出し順
- `src/test/java/pro/kensait/junit/cart/CartNestedTest.java`: ネステッドクラス
- `src/test/java/pro/kensait/junit/fee/FeeServiceTest.java`、`FeeParameterTest_2.java`、`FeeParameterTest_3.java`: 配送料のテスト（`src/main/java/.../fee/FeeService.java` がテスト対象）
- `src/test/java/pro/kensait/junit/calc/exception/CalcExceptionTest_1.java`、`CalcExceptionTest_2.java`: 例外のテスト
- `src/test/java/pro/kensait/junit/assertion4/TimeoutTest.java`、`AssumptionTest.java`: タイムアウトとアサンプション
- `src/main/java/pro/kensait/junit/customer/CustomerService2.java`、`testing/SpecialCalculator.java`: チャプター1.3 の題材
- `build.gradle`: `test` と `integrationTest` の設定

## ボタンでの動かし方

- テストクラスを開いて、エディタの右上の ファイル実行を押します。
- `scripts/01_build_test.sh` を開いて ファイル実行を押すと、ビルドテストが動きます。`ITCalculator` と `TimeoutTest` は除外されます。
- `scripts/02_integration_test.sh` を開いて ファイル実行を押すと、`ITCalculator` だけが動きます。
- `ITCalculator` や `TimeoutTest` を開いて ファイル実行を押したときは、1 クラスの指定なので除外を外して動かします（`TimeoutTest` は `@Disabled` なのでスキップになります）。

## 結果のどこを見るか

- 「テスト結果」の成功・失敗・スキップの件数と、`@DisplayName` の表示名
- 失敗したメソッドの `expected: <...> but was: <...>`
- エディターで開いたテスト対象クラスの行の色（緑・黄・赤）
- HTML のレポートは `scripts/01_build_test.sh` のあと `build/reports/tests/test/index.html` と `build/reports/jacoco/test/html/index.html` にできます
