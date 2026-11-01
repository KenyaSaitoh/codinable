# jakarta-faces-hello — FacesServlet とバッキングBean

チャプター 5.1「Jakarta Facesによる画面生成」の最初の画面です。
`web.xml` で `*.xhtml` の要求を `FacesServlet` に対応付け、`HelloPage.xhtml` の式 `#{helloBean.message}` が
バッキングBean `HelloBean` の `getMessage()` を呼んで値を表示します。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/webapp/WEB-INF/web.xml` | `FacesServlet` の登録と `*.xhtml` への対応付け、`jakarta.faces.PROJECT_STAGE` |
| `src/main/java/pro/kensait/jakartaee/hello/web/HelloBean.java` | `@Named` と `@RequestScoped` を付けたバッキングBean |
| `src/main/webapp/HelloPage.xhtml` | Facelets の画面。`h:outputText` で値を出す |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す
2. デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-hello/HelloPage.xhtml` が開き、「Hello, Alice!」が表示される

## 結果のどこを見るか

- 画面には `HelloBean` の `message` の値が出る。`@Named` により、クラス名の先頭を小文字にした `helloBean` で参照できる
- `HelloPage.xhtml` の `#{helloBean.messag}` のように名前を書き間違えて ▶ アプリ実行し直すと、
  `Development` のため、式の誤りが画面に詳しく表示される
- 最初の GET ではフォームの送信が無いので、ライフサイクルはビューの復元からレスポンスのレンダリングへ直接進む
