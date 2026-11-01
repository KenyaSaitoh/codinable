# jakarta-async-client-employee — 非同期HTTPクライアントと確認用の社員API

チャプター 3.1「HTTPクライアントとWebSocketによる通信」の前半の演習です。
Jakarta REST のクライアントAPI の `rx()` と MicroProfile Rest Client の非同期メソッドで、社員APIを呼ぶ Java コンソール（`client/`）と、
呼び出し先の確認用の社員API（このフォルダ直下）の組です。Webブラウザの画面や DB はありません。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 確認用の社員API（`http://localhost:8080/jakarta-async-client-employee/api/employees`） | ▶ アプリ実行（Payara Server 7 にデプロイ） |
| `client/` | Java コンソールのクライアント（Payara と同じ Jersey をライブラリとして使う Java SE のプログラム） | `client/run.sh` を開いてエディタの右上の ▶ 実行 |

## 見るファイル

| ファイル | 内容 |
|---|---|
| `client/src/main/java/pro/kensait/jakartaee/employee/client/Main_Employee.java` | クライアントの入口。`rx()`・`thenApply`・`thenCombine`・`thenCompose`・`exceptionally` |
| `client/src/main/java/pro/kensait/jakartaee/employee/client/EmployeeApi.java` | MicroProfile Rest Client のインタフェース（戻り値が `CompletionStage`） |
| `client/src/main/java/pro/kensait/jakartaee/employee/client/ClientOptions.java` | 接続先の決め方（第 1 引数 → 環境変数 `EMPLOYEE_API_URL` → 既定値） |
| `src/main/java/pro/kensait/jakartaee/employee/api/EmployeeResource.java` | 確認用の社員API（取得・部署／給与での検索・登録 201・更新・削除 204・404） |
| `src/main/java/pro/kensait/jakartaee/employee/api/EmployeeStore.java` | メモリ内の社員 16 件（再デプロイで初期状態に戻る） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。社員APIがデプロイされ、プレビューに社員一覧の JSON が開く
2. 社員APIを動かしたまま、`client/run.sh` を開いてエディタの右上の ▶ 実行を押す。アプリは止まらず、その脇でクライアントが動いて終わる
3. `curl/01_employees.sh` を開いて ▶ 実行すると、クライアントが呼ぶ API を curl で直接確かめられる
4. `client/test.sh` を開いて ▶ 実行すると、`ClientOptionsTest`（接続先の決め方のテスト）が流れる

## 結果のどこを見るか

`client/run.sh` の実行結果に、次の順で出ます。

- `Summary => Alice / SALES 5` … 社員 10001 の取得と部署 3 の検索を先に両方送り、`thenCombine` で合成した結果
- `Location => .../api/employees/10017` と `Updated => ...salary=250000...` … 登録の 201 で採番 ID を受け取ってから、`thenCompose` で更新を送った結果
- `Deleted => 204` … 更新の成否にかかわらず `finally` で削除する
- `Rest Client => ...` … MicroProfile Rest Client の `findAsync` で同じ社員を取得した結果（仮想スレッドの `ExecutorService` で実行）
- `Expected error => 404` … 削除済みの社員の 404 だけを `exceptionally` で `Optional.empty()` に変えた結果

社員APIを止めてから `client/run.sh` を流すと、接続の失敗が `main` まで伝わり、0 以外の終了コードで終わります。
データはメモリ内にあり、▶ アプリ実行し直すと初期状態（16 件、次の採番は 10017）に戻ります。
