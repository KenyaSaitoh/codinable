# jakarta-faces-facelets-1 — Facelets の式とデータ表示

チャプター 5.1「Jakarta Facesによる画面生成」のうち、Facelets の画面で値を出す仕組み
（値式・エスケープ・繰り返し・条件・リンク・表示形式）を確かめるサンプルです。
目次の画面 `IndexPage.xhtml` から、3 つの画面へ移動できます。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/webapp/ExpressionPage.xhtml`、`web/ExpressionBean.java` | `h:outputText`、タグの外の式、`empty` と条件演算子、`escape="false"` |
| `src/main/webapp/CollectionPage.xhtml`、`web/CollectionBean.java`、`dto/Member.java` | `ui:repeat` と `varStatus`。record の値を `#{member.memberName}` で参照する |
| `src/main/webapp/LinkPage.xhtml`、`web/UtilityBean.java` | `h:link` と `f:param`、`rendered`、`f:convertNumber`・`f:convertDateTime` |
| `src/main/webapp/MemberDetailPage.xhtml`、`web/MemberDetailBean.java` | リンクの遷移先。`f:viewParam` で `memberId` を受け取る |
| `src/main/webapp/resources/css/style.css` | `h:outputStylesheet` で読み込むリソース |

Java のクラスは `src/main/java/pro/kensait/jakartaee/facelets/` の下にあります。

## 動かし方

1. ツールバーの ▶ アプリ実行を押す
2. デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-facelets-1/IndexPage.xhtml` が開く。目次から各画面を開く

## 結果のどこを見るか

- 式とテキスト出力: ニックネームは未設定なので「(ニックネーム未設定)」が出る。`htmlText` は既定ではタグごと文字として表示され、
  `escape="false"` の行だけ太字になる
- 一覧表示: Alice・Bob・Carol の 3 件に、`status.index + 1` の番号が付く
- 条件・リンク: 管理者の Alice にだけ「（管理者）」が出る。名前のリンクは `MemberDetailPage.xhtml?memberId=1` のような URL になる
  （Webブラウザで開いたときのアドレスで確かめる）。数値は `1,000,000`、日時は `yyyy/MM/dd HH:mm:ss` の形で出る
