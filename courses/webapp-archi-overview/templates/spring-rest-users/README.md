# spring-rest-users — Web API とユニットテスト

チャプター 9.1「WebAPIによるアプリケーション連携」の演習です。
`/api/users` で一覧・1 件取得・登録・更新・削除を返す Spring Boot の REST API と、そのテストです。
データはメモリ内にあり、起動時の初期データは Alice と Bob の 2 件です。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `src/main/java/pro/kensait/spring/users/UserController.java` | 主役。`@RestController`。登録は 201 と Location、削除は 204 を返す |
| `src/main/java/pro/kensait/spring/users/GlobalExceptionHandler.java` | 見つからないときの 404、検証エラーの 400 |
| `src/main/java/pro/kensait/spring/users/UserService.java` | ユーザーの保持と操作 |
| `src/test/java/pro/kensait/spring/users/UserServiceTest.java` | UserService の単体テスト（Spring を起動しない） |
| `src/test/java/pro/kensait/spring/users/UserControllerTest.java` | `@WebMvcTest` と MockMvc による Web 層のテスト |
| `curl/01_list_users.sh` 〜 `curl/04_delete_user.sh` | API を呼ぶ curl のスクリプト |

## 動かし方

1. テスト: `UserServiceTest.java` と `UserControllerTest.java` を開いて、エディタの右上の ▶ 実行で流す。
   テスト結果タブにメソッド単位の成否が出る
2. アプリ: ▶ アプリ実行で `http://localhost:8080` に起動する（プレビューは `/api/users` の JSON）
3. アプリを起動したまま、`curl/01_list_users.sh` から順に開いて ▶ 実行で送る。
   実行中のアプリは止まらない
4. 終わったら「停止」で止める。起動し直すとデータは初期状態に戻る

## 見どころ

- 各スクリプトの先頭のコメントに期待する結果（200 / 201 と Location / 204 / 404）を書いてある
- 同じ確かめごとを、テスト（MockMvc）と curl（本物の HTTP）の両方で行っている
