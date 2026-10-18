# jakarta-faces-calc-5-conversation — 会話スコープと Flash

計算アプリケーションの 5 番目の版です。チャプター 7.1「画面遷移とスコープによる状態管理」で、
計算結果を会話スコープ（`@ConversationScoped`）のバッキングBeanに保持して次の画面で税込金額を求め、
`Conversation` の `begin()` から `end()` までを 1 つの操作として扱う仕組みを確かめます。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/calc/` の下にあります。

| ファイル | 内容 |
|---|---|
| `web/CalcConversation.java` | `add` で会話を開始し、`calcTax` で終了する。税込金額は Flash でリダイレクト先へ渡す |
| `service/CalcService.java`、`service/TaxService.java` | 足し算と、税率を掛けて 1 円未満を切り捨てる税込計算 |
| `config/CalcConfig.java`、`src/main/resources/META-INF/microprofile-config.properties` | 桁数 `4` と税率 `1.1` |
| `src/main/webapp/CalcInputPage.xhtml`、`CalcOutputPage.xhtml`、`TaxOutputPage.xhtml` | 入力画面・計算結果の画面（税込計算ボタン）・税込金額の画面 |
| `curl/01_conversation.sh` | 会話の開始から終了までを HTTP の要求で順に確かめるスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-calc-5-conversation/CalcInputPage.xhtml` が開く
2. 2 つの数値を入れて「足し算」を押し、結果画面で「税込計算」を押す
3. `curl/01_conversation.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

- 足し算の後の結果画面のアドレスには `?cid=1` のような会話ID が付く。リダイレクトをまたいでも、同じ会話の
  `CalcConversation` の値（パラメータと計算結果）が表示される
- 「税込計算」で `40.0000` は `44` になる。`calcTax` の中で会話を終了したので、税込金額の画面のアドレスには `cid` が付かない
- 終了した会話の `cid` を付けた URL（前の結果画面）を開き直すと、会話が見つからず
  `NonexistentConversationException` のエラー（500）になる（`01_conversation.sh` の最後の行）
