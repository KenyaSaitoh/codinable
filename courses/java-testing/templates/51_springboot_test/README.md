# 51_springboot_test: Spring Boot アプリケーションの単体テスト

## 何を学ぶ演習か

`@SpringBootTest` と `@MockitoBean` による Service のテスト、`@WebMvcTest` と MockMvc による Controller のテスト、テスト用のプロパティとプロファイルを確かめます（チャプター5.1）。

## 見るファイル

- `src/test/java/pro/kensait/spring/person/service/PersonServiceTest.java`: Service のテスト
- `src/test/java/pro/kensait/spring/person/web/PersonControllerTest.java`: Controller のテスト
- `src/test/java/pro/kensait/spring/config/PropertyTest.java`、`ProfileTest.java`: プロパティとプロファイル
- `src/main/java/pro/kensait/spring/person/service/PersonService.java`、`web/PersonController.java`: テスト対象

## ボタンでの動かし方

- テストクラスを開いて、エディタの右上の ファイル実行を押します。
- アプリ実行を押すと、人物管理の画面を「Webブラウザ」で動かせます。

## 結果のどこを見るか

- 「テスト結果」のメソッドごとの成否
- `PropertyTest` の実行結果の `property.value.1 => 100` と `property.value.2 => 200`
- `ProfileTest` の実行結果の `spring.datasource.url`（`application-local.yml` の値に切り替わる）
