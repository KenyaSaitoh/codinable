# spring-sec-employee-keycloak — Keycloak による OIDC ログイン

社員管理の本人確認を Keycloak（外部 IdP）へ委譲するサンプルです。OAuth 2 の認可コードフローに OIDC の ID トークンを組み合わせ、署名・issuer が検証された ID トークンの `employee_roles` に ADMIN がある社員だけに CRUD を許します。

**Keycloak は Codinable に同梱していません。** 起動には Docker Desktop が必要です。Keycloak が起動していないと、このアプリは起動時に issuer の情報を取得できずに停止します。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8085） | 実行対象 `gradle:bootRun` で「実行」 |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |
| `compose.yml`・`keycloak/` | ローカル教材用の Keycloak（8180）と realm 定義 | ターミナルで `docker compose up -d` |

## 準備（初回）

1. Docker Desktop を起動し、ターミナルで Keycloak を起動します。

   ```bash
   docker compose up -d
   ```

   管理画面は `http://localhost:8180/`（教材用 admin/admin）。最初の起動で `keycloak/learn-realm.json` が読み込まれます。

   - realm: `learn`、client: `employee-app`、issuer: `http://localhost:8180/realms/learn`
   - callback: `http://localhost:8085/login/oauth2/code/keycloak`
   - 一般社員 `alice / 1111`、管理者 `bob / 2222`
   - client role を ID トークンの `employee_roles` に出力する mapper を同梱

2. `.env.example` を `.env` という名前でコピーします。中の `KEYCLOAK_CLIENT_SECRET` は同梱 realm と対になるローカル教材専用の値です。`.env` は `application.yml` の `spring.config.import` で起動時に読み込まれます（Codinable の実行ボタンでは環境変数を渡せないため）。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押し、バックエンドを起動します。
2. ターミナルで React 画面を起動します。

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. プレビューの URL 欄に `http://localhost:5173/` を入れて開き、「keycloakでログイン」→ 認証 →「セッション確認」→「全社員を取得」の順に操作します。alice では閲覧だけ、bob では CRUD ができます。

- **ブラウザの入口は `http://localhost:5173` にそろえてください**（Vite の表示は 127.0.0.1 ですが、混在させると Cookie のホストが変わり、callback 時のセッションを見失います）。
- ログアウトはアプリのセッションだけを破棄し、Keycloak 側の SSO セッションは残ります。
- realm は既に存在すると起動時に上書きされません。設定を変えたら管理画面で反映します。
- 使い終わったら `docker compose stop` で Keycloak を止めます。

## 主なファイル

- `src/main/resources/application.yml` … OIDC クライアントの登録（client-id、scope、redirect-uri、issuer-uri）
- `src/main/java/pro/kensait/spring/employee/web/SecurityConfig.java` … oauth2Login とアクセス制御
- `src/main/java/pro/kensait/spring/employee/web/EmployeeOidcUserService.java` … ID トークンの `employee_roles` を権限へ変換
