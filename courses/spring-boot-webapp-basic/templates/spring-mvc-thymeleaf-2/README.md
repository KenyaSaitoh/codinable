# spring-mvc-thymeleaf-2 — フォーム・共通化・JavaScript 連携・メッセージ

チャプター 3.1「Spring MVCとThymeleafによる画面生成」の後半で使うサンプルです。
トップページから各ページへ移れます。

| URL | 内容 |
|---|---|
| `/fragment` | `fragments/CommonFragment.html` のフラグメントによる部品化 |
| `/layout` | `layouts/BaseLayout.html` による共通レイアウト |
| `/inline` | インライン記法と `th:inline="javascript"` による値の受け渡し |
| `/message`、`/message?lang=en` | `messages.properties` のメッセージ式とロケール切り替え（`MessageConfig`） |
| `/form` | `MemberForm` とのバインディングと `th:errors` によるエラー表示 |

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. プレビューで `http://localhost:8080/` を開く
3. `/form` で空のまま送信したり範囲外の値を入れたりして、エラー表示を確かめる

## 主なファイル

- `src/main/java/pro/kensait/spring/thymeleaf/ThymeleafController.java` … フラグメント・レイアウト・インライン・メッセージのページ
- `src/main/java/pro/kensait/spring/thymeleaf/FormController.java と dto/MemberForm.java` … /form の入力と検証
- `src/main/resources/templates/` … 上の表の各テンプレート
- `src/main/resources/messages.properties・messages_en.properties` … メッセージ
