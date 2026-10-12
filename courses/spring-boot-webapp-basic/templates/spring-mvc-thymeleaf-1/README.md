# spring-mvc-thymeleaf-1 — Thymeleaf の式とデータ表示

チャプター 3.1「Spring MVCとThymeleafによる画面生成」で使うサンプルです。
トップページから各ページへ移れます。

| URL | テンプレート | 内容 |
|---|---|---|
| `/expression` | `ExpressionPage.html` | 変数式、演算、条件式、エスケープ（`th:text` と `th:utext`） |
| `/collection` | `CollectionPage.html` | record、List、Map、繰り返し |
| `/condition` | `ConditionPage.html` | `th:if`、`th:unless`、`th:switch` |
| `/link` | `LinkPage.html` | URL 式と属性の設定 |
| `/utility` | `UtilityPage.html` | `#numbers` / `#strings` / `#temporals` / `#lists` |

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. プレビューで `http://localhost:8080/` を開き、各ページへ進む

サンプルは `thymeleaf-extras-java8time` を依存に入れていましたが、Thymeleaf 3.1 以降は
`#temporals` が本体に含まれるため、ここでは外してあります。

## 主なファイル

- `src/main/java/pro/kensait/spring/thymeleaf/ThymeleafController.java` … 各ページへ値を渡す Controller
- `src/main/resources/templates/` … 上の表の各テンプレート
