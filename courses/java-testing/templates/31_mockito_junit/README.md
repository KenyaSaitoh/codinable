# 31_mockito_junit: 荷物配送サービスへの Mockito の適用

## 何を学ぶ演習か

チャプター2.2 で手書きした `MockCostCalculator` を Mockito に置き換え、モックにする範囲の違いを比べます（レッスン3.1.5）。

## 見るファイル

- `src/test/java/pro/kensait/java/shipping/ShippingServiceTest_1.java`: 料金計算だけをモックにしたテスト
- `src/test/java/pro/kensait/java/shipping/ShippingServiceTest_2.java`: `Baggage` と `Client` までモックにしたテスト
- `src/test/java/pro/kensait/java/shipping/ShippingServiceMockingTest.java`: 手書きのテストダブルの代わりにモックを渡すテスト
- `src/main/java/pro/kensait/java/shipping/ShippingService.java`: テスト対象

## ボタンでの動かし方

テストクラスを開いて、エディタの右上の ▶ 実行を押します。

## 結果のどこを見るか

- 「テスト結果」の、ゴールド会員とダイヤモンド会員のメソッドごとの成否
- `ShippingServiceTest_2` で、`Client` のモックの地域が `null` のまま動いていること（`nullable()` が要る理由）
