# spring-resource-server-employee — リソースサーバーとして REST API を保護する

チャプター 5.1「OAuth 2・OIDC・JWTによる認証連携」の演習です。
`Authorization: Bearer <アクセストークン>` を `oauth2ResourceServer().jwt()` で検証し、`employee_roles` クレームを権限にして認可する社員 API です。
Cookie を使わないので、CSRF トークンは使いません。社員はメモリ内にあり、再起動で 16 件に戻ります。

アクセストークンの発行者には、実際の開発では Keycloak などの IdP を使います（講座の説明用の例は realm `learn` の Keycloak です）。この演習では Docker を使わずに動かせるよう、
同じ形のパスでトークンを返す教材用の簡易な発行者（`issuer` パッケージ）を同じアプリの中に置いています。
**`src/main/resources/keys/` の鍵ペアはこの演習専用です。秘密鍵を他のアプリに使ったり、実際のアプリでリソースに置いたりしないでください。**

| 利用者 | パスワード | employee_roles | 許可する操作 |
|---|---|---|---|
| alice | 1111 | BASIC | 参照 |
| bob | 2222 | ADMIN | 参照・削除 |

## 読む順番

1. `src/main/resources/application.yml` … トークンの検証の設定（公開鍵・受信者・アルゴリズム・発行者）
2. `SecurityConfig.java` … `oauth2ResourceServer` の設定、`employee_roles` から権限への変換、URL ごとの認可
3. `EmployeeApi.java` … `@AuthenticationPrincipal Jwt` でトークンのクレームを受け取る
4. `TokenIssuer.java` … 教材用の発行者（アクセストークンと ID トークンの違い）。社員 API は秘密鍵もこのクラスも使わず、公開鍵だけで検証する

## 主なファイル

| ファイル | 内容 |
|---|---|
| `src/main/resources/application.yml` | `public-key-location`・`audiences`・`jws-algorithms` と、受け入れる発行者 |
| `src/main/java/pro/kensait/spring/employee/web/SecurityConfig.java` | `oauth2ResourceServer`・`JwtAuthenticationConverter`・`JwtIssuerValidator`・`STATELESS` |
| `src/main/java/pro/kensait/spring/employee/web/EmployeeApi.java` | 社員 API（`GET /employees`・`DELETE /employees/{id}`） |
| `src/main/java/pro/kensait/spring/employee/issuer/TokenIssuer.java` | 教材用の発行者（`NimbusJwtEncoder` で RS256 の JWT を作る） |
| `src/main/java/pro/kensait/spring/employee/issuer/TokenEndpoint.java` | トークンエンドポイント（`/realms/learn/protocol/openid-connect/token`。パスワードグラントのみ） |
| `src/test/java/pro/kensait/spring/employee/web/EmployeeApiSecurityTest.java` | 401・403・200・204 と、受け入れないトークンの実例 |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す（ポート 8097）。プレビューの `/employees` はトークンが無いので 401 になる
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で流す
   - `01_access_token.sh` … トークンなし 401、alice の参照 200、alice の削除 403、bob の削除 204、ID トークンを送ると 401、署名を書き換えると 401
   - `02_token_claims.sh` … bob のアクセストークンのヘッダーとペイロードを Base64URL から戻して表示する
3. `EmployeeApiSecurityTest.java` を開いて、エディタの右上の ▶ 実行で流す

`01_access_token.sh` を 2 回流すと、bob の削除は 404 になります（社員 10016 を削除済みで、認可は通っている）。▶ アプリ実行し直すと戻ります。

## 結果のどこを見るか

- 401 は「トークンが無い・検証に失敗した」、403 は「検証は通ったが権限が足りない」。トークンの検証と操作の許可は別の段階
- 署名の正しいトークンでも、ID トークンは `aud` に `employee-api` を含まないので 401 になる（用途の違うトークンを受け入れない）。`application.yml` の `audiences` を消すと通ってしまう
- `02_token_claims.sh` のとおり、署名付き JWT の中身は誰でも読める。署名は改ざんの検出のためで、秘匿のためではない
- `EmployeeApiSecurityTest` の `otherKeyIsRejected`・`otherIssuerIsRejected`・`expiredTokenIsRejected` … 別の鍵の署名、別の発行者、期限切れも 401。`validHandMadeTokenIsAccepted` は同じ組み立てで正しいものが通ることを示す
- bob の削除のあと、実行結果に `社員を削除しました id=10016 by=bob sub=...` が出る。`by` と `sub` はトークンのクレーム

## Keycloak を使う場合との違い

`application.yml` の `public-key-location` と `employee.jwt.expected-issuer` の代わりに `issuer-uri` を書きます。発行者の OIDC の設定から JWKS の場所を見つけ、`iss` の検証も自動で加わります。`issuerValidator` の Bean は要らなくなります。
