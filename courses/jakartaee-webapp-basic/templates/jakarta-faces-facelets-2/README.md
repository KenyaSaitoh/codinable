# jakarta-faces-facelets-2 — フォーム部品・テンプレート・リソース・メッセージバンドル

チャプター 5.1「Jakarta Facesによる画面生成」のうち、選択肢を持つ入力部品、テンプレートによる HTML の共通化、
リソース（CSS・JavaScript）、メッセージバンドルとロケールの切り替えを確かめるサンプルです。
目次の画面 `IndexPage.xhtml` から、4 つの画面へ移動できます。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/webapp/FormPage.xhtml`、`FormResultPage.xhtml`、`web/MemberFormBean.java` | `h:selectOneRadio`・`h:selectOneMenu`・`h:selectManyCheckbox`・`h:inputTextarea` と `f:selectItems` |
| `src/main/webapp/WEB-INF/templates/BaseLayout.xhtml`、`WEB-INF/includes/PageHeader.xhtml`、`LayoutPage.xhtml` | `ui:insert`・`ui:define`・`ui:include` によるテンプレート |
| `src/main/webapp/InlinePage.xhtml`、`resources/js/member.js`、`web/InlineBean.java` | `pt:` 属性で値を `data-member-name` に出し、JavaScript で読む |
| `src/main/webapp/MessagePage.xhtml`、`web/LocaleBean.java`、`web/MessageBean.java` | `h:outputFormat` と `f:view locale` によるロケールの切り替え |
| `src/main/webapp/WEB-INF/faces-config.xml`、`src/main/resources/messages*.properties` | メッセージバンドル `msg` の登録と、日本語・英語の文言 |

Java のクラスは `src/main/java/pro/kensait/jakartaee/facelets/` の下にあります。

## 動かし方

1. ツールバーの ▶ アプリ実行を押す
2. デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-facelets-2/IndexPage.xhtml` が開く。目次から各画面を開く

## 結果のどこを見るか

- 選択肢を持つ入力部品: 性別・都道府県・趣味を選んで「登録する」を押すと、結果画面には表示ラベルではなく送信値
  （`MALE`、`TOKYO`、`[READING, TRAVEL]` など）が出る
- テンプレート: `LayoutPage.xhtml` は本文しか書いていないが、ヘッダー・フッター・CSS の付いた 1 つの HTML になる
- JavaScript への値の受け渡し: 「Hello, Alice!」は、サーバーが出した属性の値を `member.js` が書き込んだもの
- メッセージバンドル: 「English」「日本語」を押すと文言が切り替わる。選んだ言語はセッションスコープの `LocaleBean` に残る
