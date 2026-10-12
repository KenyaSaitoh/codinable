# mockito-shipping：依存先のモックと副作用の検証（チャプター2.1）

配送注文の処理（`ShippingService`）を Mockito で検証します。
料金問い合わせ（`ShippingRateClient`）と保存先（`ShippingRepository`）をモックにし、
`Clock.fixed` で注文時刻を固定して、戻り値と保存内容を確かめます。Spring も DB も使いません。

## Codinable での動かし方

| 操作 | 内容 |
|---|---|
| `ShippingServiceTest.java` を開いて、エディタの右上の ▶ 実行 | テストを流し、テスト結果にケースごとの成否を表示 |
| ツールバーの ▶ アプリ実行 | 固定料金を返すクライアントとメモリ内保存を使い、3,600円の配送注文を1件保存する例を出す |

| テスト | 確かめること |
|---|---|
| `savesDiscountedOrderWithFixedTime` | GOLD 会員・基本料金4,000円で、3,600円と固定時刻の注文を保存し、同じ注文を返す（`ArgumentCaptor`） |
| `doesNotSaveWhenRateServiceFails` | 料金取得が例外を返したら保存しない（`verifyNoInteractions`） |
| `rejectsInvalidOrderBeforeCallingDependencies` | 入力が不正なら料金の問い合わせも保存もしない |

JDK 25 で Mockito を動かすため、`build.gradle` でテスト実行時に `mockito-core` を `-javaagent` として渡しています。

## ファイル

- `src/main/java/pro/kensait/shipping/ShippingService.java`：入力確認 → 料金取得 → 割引 → 保存
- `src/test/java/pro/kensait/shipping/ShippingServiceTest.java`：モック・Captor・固定 Clock のテスト
