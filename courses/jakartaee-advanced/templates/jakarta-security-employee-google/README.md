# jakarta-security-employee-google — Google による OpenID Connect のログイン

チャプター 5.1「OpenID Connect・JWTによる認証連携」の Google 版の演習です。
`@OpenIdAuthenticationMechanismDefinition` で Google を IdP にし、認可コードフローでログインします。
Google の ID トークンには `employee_roles` も `preferred_username` も無いので、呼び出し元の名前には `sub` を使い、
グループは `BasicGroupIdentityStore` が補う BASIC だけです（Google で認証されたという理由で ADMIN は与えません）。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 社員API（`http://localhost:8080/jakarta-security-employee-google/api`） | ▶ アプリ実行 |
| `frontend/` | React の画面（Vite、5173）。`/api` を 8080 の `/jakarta-security-employee-google/api` へ転送する | ▶ アプリ実行で一緒に起動 |

## 準備（Google 側の登録。初回だけ）

ログインまで試すには、Google Cloud のコンソールで OAuth クライアント（種類は「ウェブ アプリケーション」）を登録します。

1. 承認済みのリダイレクト URI に `http://localhost:8080/jakarta-security-employee-google/callback` を登録する
2. 発行されたクライアントIDとシークレットを、`payara/post-boot-commands.asadmin` の 2 行の `<...>` に書き、行頭の `#` を外す
   （Codinable の実行ボタンでは環境変数を渡せないため、Payara の設定ソースに入れる。秘密の値なので、このファイルを他人に渡さない）

登録しなくてもデプロイはでき、「Googleでログイン」で Google の画面へ移るところまでは動きます（その先は Google が `client_id` を拒否します）。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/security/SecurityConfig.java` | `@OpenIdAuthenticationMechanismDefinition`（`callerNameClaim = "sub"`） |
| `src/main/java/pro/kensait/jakartaee/employee/security/OidcConfig.java` | MicroProfile Config から読む発行者・クライアントID・シークレット |
| `src/main/java/pro/kensait/jakartaee/employee/security/BasicGroupIdentityStore.java` | 認証済みの呼び出し元に BASIC を補う（`PROVIDE_GROUPS` だけ） |
| `src/main/java/pro/kensait/jakartaee/employee/web/SessionResource.java` | `OpenIdContext` で読む `sub`・メールアドレスとロール |
| `src/main/java/pro/kensait/jakartaee/employee/web/LoginServlet.java` | 保護された `/login`。ログイン後に React の画面へ戻す |
| `src/main/webapp/WEB-INF/web.xml` | 認可の制約（チャプター4.1と同じ）と、セッション Cookie `GOOGLE_SESSION` |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。社員API が Payara Server 7 にデプロイされ、React の画面（5173）が開く
2. 「Googleでログイン」を押すと、保護された `/login` から Google のログイン画面へ転送される。
   ログインすると `/callback` で認可コードがトークンに交換され、`/login` を経て React の画面へ戻る
3. ログイン中の名前（`sub` の値）とメールアドレス、ロール（BASIC）が出る。社員一覧は見えるが、「削除」はサーバーが 403 で拒否する

## 結果のどこを見るか

- 未ログインで `/api/employees` を開くと、401 ではなく Google の認可エンドポイントへの 302 が返る（OIDC の認証メカニズムがログインを始める）。
  その URL に `client_id`・`redirect_uri`・`state`・`nonce` が付いている
- Webブラウザの入口は `localhost` にそろえる。`127.0.0.1` と混在すると Cookie のホストが変わり、コールバックで `state` を照合できない
- ログアウトは社員管理システム側のセッションを破棄するだけで、Google 側のログイン状態は残る
