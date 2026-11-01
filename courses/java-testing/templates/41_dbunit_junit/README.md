# 41_dbunit_junit: DBUnit と Mockito を組み合わせたテスト

## 何を学ぶ演習か

料金計算を Mockito のモックで固定し、`ShippingService` の結果を SHIPPING テーブルの中身で確かめます（レッスン4.1.4）。

## 見るファイル

- `src/test/java/pro/kensait/java/shipping/ShippingServiceDBTest.java`: テストクラス
- `src/main/java/pro/kensait/java/shipping/ShippingService.java`、`ShippingDAO.java`: テスト対象
- `src/test/resources/EXPECTED_DATA/SHIPPING.csv`: 期待値（ID 1、顧客ID 30001、受取日 2023-11-30、荷物 1 個、合計 1600 円）

## ボタンでの動かし方

`ShippingServiceDBTest.java` を開いて、エディタの右上の ファイル実行を押します。DB はインメモリの HSQLDB で、テーブルはテストの開始時に `src/test/resources/schema.sql` から作られます。

## 結果のどこを見るか

- 「テスト結果」の成否。注文日時（ORDER_DATE_TIME）は比較から除外しています
- テストの前処理の `ALTER TABLE SHIPPING ALTER COLUMN ID RESTART WITH 1`。消すと、2 回目以降に ID がずれます

## この演習で使うテーブル

SHIPPING（ID 自動採番の主キー、ORDER_DATE_TIME、CLIENT_ID、RECEIVE_DATE、BAGGAGE_COUNT、TOTAL_PRICE）。初期データはありません。
