# spring-mvc-hello — Controller・Model・View の最小構成

チャプター 3.1「Spring MVCとThymeleafによる画面生成」の最初のサンプルです。
`HelloController` が Model に `message` を入れてビュー名 `HelloPage` を返し、
Thymeleaf の `templates/HelloPage.html` が `th:text="${message}"` で表示します。

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. プレビューの URL に `http://localhost:8080/hello` を入れて開く

`HelloController` の Model の値やテンプレートの属性名を書き換え、▶ アプリ実行で起動し直して表示の変化を確かめてください。

## 主なファイル

- `src/main/java/pro/kensait/spring/hello/HelloController.java` … Model に値を入れてビュー名を返す
- `src/main/resources/templates/HelloPage.html` … th:text で表示するテンプレート
