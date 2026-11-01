# jakarta-faces-calc-4-prg — faces-redirect=true による PRG と Flash

計算アプリケーションの 4 番目の版です。チャプター 7.1「画面遷移とスコープによる状態管理」で、
処理の POST と表示の GET を分ける PRG（Post/Redirect/Get）と、リダイレクト先へ値を 1 回だけ持ち越す Flash を確かめます。
入力値の制約と変換はチャプター 6.1 の `jakarta-faces-calc-2-validation` と同じです。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/calc/web/CalcBean.java` | 計算結果を `flash.put("result", …)` で Flash に入れ、`CalcOutputPage?faces-redirect=true` を返す |
| `src/main/webapp/CalcOutputPage.xhtml` | `#{flash.result}` で Flash の値を読む結果画面 |
| `src/main/webapp/CalcInputPage.xhtml` | 入力画面（足し算・引き算） |
| `curl/01_prg.sh` | POST → 302 → GET → もう一度 GET の流れを確かめるスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-calc-4-prg/CalcInputPage.xhtml` が開く
2. 2 つの数値を入れて「足し算」を押す。結果画面が出たら、Webブラウザで再読み込みする
3. `curl/01_prg.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

- 「足し算」の後、アドレスは `CalcOutputPage.xhtml` に変わる（`faces-redirect=true` によるリダイレクト。ステータスは 302）
- 結果画面を再読み込みすると、POST は再送されず、計算結果は空になる。Flash の値はリダイレクト後の 1 回の描画で破棄される
- `CalcBean` は `@RequestScoped` なので、リダイレクト後の GET では別のインスタンスになり、`param1`・`param2` は引き継がれない。
  届くのは Flash に入れた値だけ
