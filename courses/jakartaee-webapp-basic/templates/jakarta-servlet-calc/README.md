# jakarta-servlet-calc — HttpServletRequest と HttpServletResponse

チャプター 2.1「Jakarta EEとサーブレットによるHTTP処理」の計算アプリケーションです。
入力画面は WAR のルートに置いた静的な `index.html` で、`CalcServlet` が `/add`（POST）と `/addByGet`（GET）の
両方を受け、`getParameter` で受け取った 2 つの値を足して、結果の HTML を `HttpServletResponse` へ書き出します。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/webapp/index.html` | 入力画面。2 つのボタンが `formmethod` / `formaction` で POST と GET を出し分ける |
| `src/main/java/pro/kensait/jakartaee/calc/web/CalcServlet.java` | `/add` と `/addByGet` を受け持つサーブレット。`doPost` と `doGet` が同じ `calculate` を呼ぶ |
| `src/main/webapp/WEB-INF/web.xml` | デプロイ記述子。文字コード・ウェルカムファイル・セッションの有効期限 |
| `curl/01_add_post.sh` 〜 `curl/04_not_number.sh` | HTTP の要求を直接送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-servlet-calc/` の入力画面が開く
2. パラメータ1・2 に数値を入れて「足し算(POST)」「足し算(GET)」を押す
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で 1 つずつ流す

## 結果のどこを見るか

- 「足し算(GET)」では、Webブラウザのアドレスに `?param1=…&param2=…` が付く。POST では本文で送られるので付かない
- `03_missing_param.sh` は `param2` を送らないので、`sendError` の 400 が返る
- `04_not_number.sh` は `Double.parseDouble` が `NumberFormatException` を送出し、コンテナが 500 を返す。
  実行結果に例外のスタックトレースが出る
- 計算結果は `calculate` のローカル変数 `result` に置いている。サーブレットのインスタンスは 1 つで、
  インスタンス変数はすべてのリクエストで共有されるため
