# junit-shipping：配送料金の境界値と例外（チャプター2.1）

荷物配送サービスの配送料金計算（`ShippingFeePolicy`）を JUnit Jupiter と AssertJ で検証します。
Spring も DB も使わない、通常の Java プロジェクトです。

## 会員区分と割引

| 会員区分 | 割引 | 割引後の下限額 |
|---|---:|---:|
| REGULAR | 割引なし | なし |
| GOLD | 基本料金の90% | 3,000円 |
| DIAMOND | 基本料金の75% | 2,500円 |

基本料金が下限額以下ならそのまま返し、超える場合は割引して1円未満を四捨五入し、下限額と比べて大きい方を返します。

## Codinable での動かし方

| 実行対象 | 内容 |
|---|---|
| `test`（既定） | `ShippingFeePolicyTest` を実行し、テスト結果にケースごとの成否を表示 |
| `run` | `ShippingApplication` で GOLD 会員・基本料金4,000円の割引例（3,600円）をコンソールに出す |

`@ParameterizedTest` の各行（例：`"GOLD, 3334, 3001"`）が別々のテストケースとして結果に並びます。
`@CsvSource` の期待値や `ShippingFeePolicy` の丸め方を書き換えて、どのケースが失敗するかを確かめてください。

## ファイル

- `src/main/java/pro/kensait/shipping/ShippingFeePolicy.java`：割引と下限額の計算
- `src/test/java/pro/kensait/shipping/ShippingFeePolicyTest.java`：境界値・四捨五入・例外のテスト
