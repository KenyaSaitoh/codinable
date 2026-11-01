# jakarta-mpjwt-employee — MicroProfile JWT による REST API の保護

チャプター 5.1「OpenID Connect・JWTによる認証連携」のレッスン 5.1.5 の演習です。
`Authorization: Bearer <アクセストークン>` を MicroProfile JWT で検証し、`groups` クレームをロールにして `@RolesAllowed` で認可する
リソースサーバーです。Cookie を使わないので、CSRF トークンは使いません。

アクセストークンの発行者は、講座本編では Keycloak（realm: `learn`）ですが、この演習では Docker を使わずに動かせるよう、
同じ形のパスで JWKS とトークンを返す教材用の簡易な発行者（`issuer/`）を一緒にデプロイします。
発行者の URI は `http://localhost:8080/jakarta-mpjwt-issuer/realms/learn` です。
そのため `microprofile-config.properties` の `mp.jwt.verify.publickey.location` と `mp.jwt.verify.issuer` だけは、講座のコード
（Keycloak の `http://localhost:8180/realms/learn`）と違い、この発行者の URL を指しています。ほかの設定とコードは講座と同じです。

| 場所 | 中身 | 動かし方 |
|---|---|---|
| このフォルダ直下 | 社員API（`http://localhost:8080/jakarta-mpjwt-employee/api/employees`） | ▶ アプリ実行 |
| `issuer/` | 教材用の発行者（JWKS と RS256 の JWT。利用者は alice / 1111 と bob / 2222） | ▶ アプリ実行で一緒に同じ Payara Server にデプロイ |

| 利用者 | パスワード | groups | 許可する操作 |
|---|---|---|---|
| alice | 1111 | BASIC | 参照 |
| bob | 2222 | ADMIN | 参照・削除 |

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/resources/META-INF/microprofile-config.properties` | 検証の設定（JWKS の取得先・発行者・受信者・アルゴリズム・時刻のずれ） |
| `src/main/java/pro/kensait/jakartaee/employee/web/JaxrsActivator.java` | `@LoginConfig(authMethod = "MP-JWT")` と `@DeclareRoles` |
| `src/main/java/pro/kensait/jakartaee/employee/web/EmployeeResource.java` | `JsonWebToken`・`@Claim` の注入と `@RolesAllowed` |
| `src/main/webapp/WEB-INF/beans.xml` | Payara の場合、`@LoginConfig` のクラスを CDI の Bean にするための `bean-discovery-mode="all"` |
| `issuer/src/main/java/pro/kensait/jakartaee/issuer/TokenIssuer.java` | 教材用の発行者（鍵ペアの生成、JWT の署名、JWKS） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。発行者と社員API が Payara Server 7 にデプロイされる。プレビューの `/api/employees` はトークンが無いので 401 になる
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で流す
   - `01_access_token.sh` … トークンなし 401、alice の参照 200、alice の削除 403、bob の削除 204、ID トークンを送ると 401、署名を書き換えると 401
   - `02_token_claims.sh` … bob のアクセストークンのヘッダーとペイロードを Base64URL から戻して表示する

## 結果のどこを見るか

- 署名の正しいトークンでも、ID トークンは `aud` に `employee-api` を含まないので 401 になる（用途の違うトークンを受け入れない）
- 401 は「トークンが無い・検証に失敗した」、403 は「検証は通ったがロールが足りない」。トークンの検証と操作の許可は別の段階
- `02_token_claims.sh` のとおり、署名付き JWT の中身は誰でも読める。署名は改ざんの検出のためで、秘匿のためではない
- 発行者は起動のたびに鍵ペアを作り直す。▶ アプリ実行し直すと、前に取ったトークンは署名の検証に失敗する
