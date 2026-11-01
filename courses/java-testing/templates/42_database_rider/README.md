# 42_database_rider: Database Rider による宣言的なテスト

## 何を学ぶ演習か

チャプター4.1 と同じ `EmployeeDAO` のテストを、`@DataSet` と `@ExpectedDataSet` と YAML のデータセットで書きます（チャプター4.2）。Database Rider が内部で使う DBUnit は 2.7.3 なので、`41_dbunit` とは別のプロジェクトにしています。

## 見るファイル

- `src/test/java/pro/kensait/jdbc/company/EmployeeDAOTest.java`: テストクラス
- `src/test/resources/datasets/init.yml`: 初期データ
- `src/test/resources/datasets/expected_update.yml`: 一括更新の期待値（4 列だけを比べる）
- `src/test/resources/dbunit.yml`: 接続情報
- `build.gradle`: `rider-junit5` の依存関係

## ボタンでの動かし方

`EmployeeDAOTest.java` を開いて、エディタの右上の ファイル実行を押します。接続先は講座本編の HSQLDB サーバーではなくインメモリの HSQLDB（`jdbc:hsqldb:mem:testdb`）にしてあるので、サーバーの起動は要りません。テーブルはテストの開始時に `src/test/resources/schema.sql` から作られます。

## 結果のどこを見るか

- 「テスト結果」のメソッドごとの成否
- `expected_update.yml` の値を書き換えたときの失敗のメッセージ
- `expected_update.yml` に無い JOB_NAME は比べられないこと

## この演習で使うテーブル

DEPARTMENT（DEPARTMENT_NAME 主キー）と EMPLOYEE（EMPLOYEE_ID 主キー、EMPLOYEE_NAME、DEPARTMENT_NAME は DEPARTMENT への外部キー、ENTRANCE_DATE、JOB_NAME、SALARY）。初期データは `init.yml` です。
