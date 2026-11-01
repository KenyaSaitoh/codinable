# jakarta-security-employee-faces — Faces のフォーム認証と protected-views

チャプター 4.1「Jakarta Securityの認証・認可とリクエスト保護」の、ログイン画面を Jakarta Faces で作る場合の演習です。
`@CustomFormAuthenticationMechanismDefinition` によるカスタムフォーム認証と、`faces-config.xml` の `protected-views` による
GET で開く画面の CSRF 対策を確かめます。照合先（データベースのアイデンティティストア）とアカウントは `jakarta-security-employee` と同じです。

| ユーザー名 | 教材用パスワード | グループ（ロール） | できること |
|---|---|---|---|
| Alice | 1111 | BASIC | 社員一覧の閲覧 |
| Bob | 2222 | ADMIN | 閲覧と削除 |

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/security/SecurityConfig.java` | `@CustomFormAuthenticationMechanismDefinition`（`loginPage`・`errorPage`・`useForwardToLogin`） |
| `src/main/java/pro/kensait/jakartaee/employee/security/IdentityStoreConfig.java` | `@DatabaseIdentityStoreDefinition`（PBKDF2） |
| `src/main/java/pro/kensait/jakartaee/employee/web/LoginBean.java` | `SecurityContext.authenticate` の戻り値（`SEND_CONTINUE`・`SEND_FAILURE`・`SUCCESS`）による分岐 |
| `src/main/webapp/employees/list.xhtml` | 社員一覧。削除のリンクは `rendered="#{request.isUserInRole('ADMIN')}"` で表示を切り替える |
| `src/main/webapp/employees/delete.xhtml` | 開くだけで確認へ進む削除画面 |
| `src/main/webapp/WEB-INF/faces-config.xml` | `protected-views` |
| `src/main/webapp/WEB-INF/web.xml` | 画面の URL に対するセキュリティ制約（削除画面は ADMIN だけ） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。プレビューで社員一覧を開こうとすると、未ログインなのでログイン画面へリダイレクトされる
2. Bob / 2222 でログインすると、元の社員一覧へ戻る。各行の「削除」から確認画面を開き、「削除する」で削除できる
3. 誤ったパスワードでは、ログイン画面に「ユーザー名またはパスワードが正しくありません」が出る
4. `curl/01_login_flow.sh` を開いてエディタの右上の ▶ 実行を押すと、リダイレクト、トークン付きのリンク、
   トークンの無い URL の拒否、Alice での 403 を順に確かめられる

## 結果のどこを見るか

- 削除のリンクの URL に `jakarta.faces.Token` が付いている。同じ削除画面の URL をトークン無しで開くと表示できない（500。`ProtectedViewException`。Webブラウザには `web.xml` の `error-page` で理由を示す画面が出る）
- Alice には削除のリンクが表示されないが、それは表示の制御にすぎない。削除画面の URL を直接開いても、`web.xml` の制約で 403 になる
- 社員データはメモリ内にあり、▶ アプリ実行し直すと 16 件に戻る
