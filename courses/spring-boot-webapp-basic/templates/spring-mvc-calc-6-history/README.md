# spring-mvc-calc-6-history — ID 付き URL と保存済みデータの再取得

チャプター 5.1「リダイレクトとセッションによる状態管理」（5.1.3）で使うサンプルです。
クエリパラメータとパス変数の例（`/viewResultByQuery?id=1`、`/viewResultByPath/1`）は
チャプター 4.1（4.1.1）でも取り上げます。

- 計算結果は `CalcDAO` がメモリー上に履歴として保存し、`CalcService` は保存した ID を返す
- Controller は `RedirectAttributes.addAttribute("id", id)` を付けて `redirect:/viewResult` を返す（URL は `/viewResult?id=...` になる）

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開き、何回か計算する
3. `/viewResult?id=1` を開き直し、同じ結果を何度でも取得できることを確かめる
4. `/viewHistory1`・`/viewHistory2`・`/viewHistory3` で履歴一覧を、
   `/viewResultByQuery?id=1`・`/viewResultByPath/1` で ID 指定の表示を確かめる

履歴はメモリー上にあるので、停止すると消えます。
