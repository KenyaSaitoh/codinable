# 22_junit_dependency: 依存性注入とテストダブル

## 何を学ぶ演習か

荷物配送サービスの `ShippingService` を題材に、依存性注入で料金計算をテストダブルに置き換える方法と、テストしにくい依存の見分け方を確かめます（チャプター2.2）。JaCoCo のカバレッジの読み方（チャプター2.3）もこの題材で確かめます。

## 見るファイル

- `src/main/java/pro/kensait/java/shipping/ShippingService.java`: テスト対象。割引の分岐、`LocalDateTime.now()`、`ShippingDAO.save()`
- `src/main/java/pro/kensait/java/shipping/Shipping.java`: `equals()` の比較の対象
- `src/test/java/pro/kensait/java/shipping/MockCostCalculator.java`: 常に 1600 を返すテストダブル
- `src/test/java/pro/kensait/java/shipping/ShippingServiceTest.java`: 本物の `CostCalculator` を注入するテスト
- `src/test/java/pro/kensait/java/shipping/ShippingServiceMockingTest.java`: テストダブルを注入するテスト

## ボタンでの動かし方

- `ShippingServiceTest.java` と `ShippingServiceMockingTest.java` を開いて、エディタの右上の ▶ 実行を押します。
- `scripts/01_jacoco_report.sh` を開いて ▶ 実行を押すと、JaCoCo のレポートを作ります（先に一度テストクラスを ▶ 実行しておきます）。

## 結果のどこを見るか

- 「テスト結果」の、ゴールド会員とダイヤモンド会員のネステッドクラスごとの成否と、カバレッジの割合
- `ShippingService.java` を開いたときの行の色。緑は実行された行、黄は分岐の一部だけが実行された行、赤は実行されなかった行です
- JaCoCo の HTML レポートは `build/reports/jacoco/test/html/index.html`、XML は `build/reports/jacoco/test/jacocoTestReport.xml` にできます。HTML では Missed Instructions が命令網羅、Missed Branches が分岐網羅です
