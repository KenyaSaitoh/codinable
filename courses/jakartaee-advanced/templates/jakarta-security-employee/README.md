# jakarta-security-employee — Jakarta Security の認証・認可とリクエスト保護

チャプター 4.1「Jakarta Securityの認証・認可とリクエスト保護」の演習です。
独自の認証メカニズムとデータベースのアイデンティティストアによるログイン、`web.xml` の URL・HTTP メソッド・ロールによる認可、
PBKDF2 によるパスワードのハッシュ化、CSRF トークンとセッションの保護を、社員API で確かめます。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 社員API（`http://localhost:8080/jakarta-security-employee/api`） | ▶ アプリ実行（Payara Server 7 にデプロイ） |
| `frontend/` | React の画面（Vite、5173）。`/api` を 8080 の `/jakarta-security-employee/api` へ転送する | ▶ アプリ実行で一緒に起動 |

## ログインのアカウント

| ユーザー名 | 教材用パスワード | グループ（ロール） | 許可する社員操作 |
|---|---|---|---|
| Alice | 1111 | BASIC | 閲覧 |
| Bob | 2222 | ADMIN | 閲覧・登録・更新・削除 |

アカウントは起動時に `UserInitializer` が Payara Server 既定の DB（`java:comp/DefaultDataSource`）の `APP_USER`・`APP_USER_GROUP` に、
ハッシュ化したパスワードで登録します。社員データ（Alice〜Trent の 16 件）はメモリ内にあり、ログイン用のアカウントとは別です。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/security/EmployeeAuthenticationMechanism.java` | 401 を返す独自の認証メカニズム（`@AutoApplySession`、`changeSessionId`） |
| `src/main/java/pro/kensait/jakartaee/employee/security/SecurityConfig.java` | `@DatabaseIdentityStoreDefinition`（PBKDF2、60 万回） |
| `src/main/java/pro/kensait/jakartaee/employee/security/UserInitializer.java` | 起動時のアカウント登録（`Pbkdf2PasswordHash.generate`） |
| `src/main/java/pro/kensait/jakartaee/employee/web/LoginResource.java` | `SecurityContext.authenticate` によるログイン（204／401） |
| `src/main/java/pro/kensait/jakartaee/employee/web/EmployeeResource.java` | 社員API と `@RolesAllowed` |
| `src/main/java/pro/kensait/jakartaee/employee/web/CsrfFilter.java`・`CsrfResource.java` | CSRF トークンの発行と照合 |
| `src/main/java/pro/kensait/jakartaee/employee/web/CorsFilter.java` | 5173 から直接呼ぶ構成のための CORS の応答ヘッダー（この演習の画面では働かない） |
| `src/main/webapp/WEB-INF/web.xml` | `security-constraint`・`deny-uncovered-http-methods`・`session-config` |
| `payara/post-boot-commands.asadmin` | Payara の場合の、グループ名をそのままロールにする設定と、セッション Cookie の `SameSite=Lax` |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。社員API が Payara Server 7 にデプロイされ、React の画面（5173）が開く
2. Bob / 2222 でログインすると、社員一覧と登録フォームが出る。登録・削除ができる
3. ログアウトして Alice / 1111 でログインし、「登録」を押すと 403 になる（画面はボタンを隠していない。許可はサーバーが判定する）
4. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で順に流す
   - `01_unauthenticated.sh` … 未ログインの GET・POST は 401、`/api/csrf` は 200
   - `02_login_alice.sh` … 誤ったパスワードは 401、Alice の参照は 200、登録は 403（ロール不足）
   - `03_login_bob.sh` … Bob の CSRF トークンなしの登録は 403、トークン付きは 201、PATCH は 403、ログアウト後は 401

## 結果のどこを見るか

- ログインの POST も変更要求なので、先に `GET /api/csrf` でトークンを取る。ログインに成功するとトークンは破棄され、次の変更操作の前に取り直す
- 未ログインの POST は、CSRF の検証より先にコンテナの判定で 401 になる。403 は「ロール不足」と「CSRF トークンの不一致」の両方で返るので、条件を 1 つずつ変えて確かめる
- ログインのたびに `SECURITY_SESSION` の値が変わる（`changeSessionId` によるセッション固定化の対策）
- `Set-Cookie: SECURITY_SESSION=...; HttpOnly;SameSite=Lax;SameSite=Lax` の `SameSite=Lax` は、講座のとおり `web.xml` の `cookie-config` に書いてある。
  ただし Payara の場合（7.2026.9 で確認）、Servlet 6.0 以降の `<attribute>` を Set-Cookie に出さないため、
  `payara/post-boot-commands.asadmin` で Payara Server の HTTP の設定からも同じ値を付けている（Payara Server は 2 回続けて書くが、値は同じ）
