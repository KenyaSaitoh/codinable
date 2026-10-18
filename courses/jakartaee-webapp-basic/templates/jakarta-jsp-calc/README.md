# jakarta-jsp-calc — JSP によるフォワードとセッションのスコープ

チャプター 3.1「JSPとMVCパターンによる画面生成」の計算アプリケーションです。
`CalcServlet` が `/calc`（GET）で入力画面の JSP へ、`/add`（POST）で計算してから結果画面の JSP へフォワードします。
計算結果はリクエストの属性 `result` と、セッションの属性 `lastResult` の 2 か所に置きます。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/calc/web/CalcServlet.java` | Controller。`setAttribute` で値を置き、`getRequestDispatcher(...).forward` で JSP へ渡す |
| `src/main/java/pro/kensait/jakartaee/calc/service/CalcService.java` | 計算だけを受け持つクラス（状態を持たない） |
| `src/main/webapp/WEB-INF/views/calcInput.jsp` | 入力画面。`sessionScope.lastResult` があれば前回の計算結果を出す |
| `src/main/webapp/WEB-INF/views/calcOutput.jsp` | 結果画面。`${result}` を `c:out` で出す |
| `src/main/webapp/WEB-INF/web.xml` | `scripting-invalid` でスクリプトレットを禁止している |
| `curl/01_forward.sh` 〜 `curl/03_direct_jsp.sh` | HTTP の要求を直接送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-jsp-calc/calc` が開く
2. 2 つの数値を入れて「足し算」を押し、結果画面の「入力画面へ戻る」で戻る
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で 1 つずつ流す

## 結果のどこを見るか

- 結果画面のアドレスは `/add` のまま。フォワードはサーバーの中の受け渡しで、Webブラウザは知らない
- 入力画面へ戻ると「前回の計算結果」が出る。別のリクエストでも、同じセッションの属性は残っている
- `02_session.sh` は、Cookie（`JSESSIONID`）を持ち回った要求では前回の結果が出て、Cookie なしでは出ないことを示す
- `03_direct_jsp.sh` は `WEB-INF` の下の JSP を直接要求して 404 になる。JSP を開く入口はサーブレットだけ
- JSP は最初の要求のときにサーブレットへ変換・コンパイルされる。最初の表示だけ少し時間がかかる
