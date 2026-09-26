# spring-sec-employee-google — Google による OIDC ログイン

社員管理の本人確認を Google へ委譲するサンプルです。Google ログインの成功は本人確認であって管理者の根拠ではないため、認証された利用者には閲覧（BASIC）だけを許します。管理権限の確認は Keycloak 版やフォーム認証版で行います。

**自分の Google Cloud プロジェクトで作成した OAuth クライアントが必要です。** クライアント ID とシークレットが無いと、このアプリは起動できません。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8084） | 実行対象 `gradle:bootRun` で「実行」 |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |

## 準備（初回）

1. Google Cloud の「OAuth クライアント ID」を「ウェブアプリケーション」として作成し、承認済みのリダイレクト URI に `http://localhost:8084/login/oauth2/code/google` を登録します。テストモードの場合は、使う Google アカウントをテストユーザーに追加します。
2. `.env.example` を `.env` という名前でコピーし、`GOOGLE_CLIENT_ID` と `GOOGLE_CLIENT_SECRET` に自分の値を書きます。`.env` は `application.yml` の `spring.config.import` で起動時に読み込まれます（Codinable の実行ボタンでは環境変数を渡せないため）。`.env` は秘密の値を含むので、他人に渡さないでください。

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押し、バックエンドを起動します。
2. ターミナルで React 画面を起動します。

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. プレビューの URL 欄に `http://localhost:5173/` を入れて開き、Google でログインしてから社員一覧を取得します。更新系の操作は拒否されます。

- **ブラウザの入口は `http://localhost:5173` にそろえてください**（Vite の表示は 127.0.0.1 ですが、混在させると Cookie のホストが変わり、callback 時のセッションを見失います）。Google のログイン画面がプレビューで開けない場合は、外部ブラウザで同じ URL を開きます。
- ログアウトはアプリのセッションだけを破棄し、Google 側のセッションは残ります。

## 主なファイル

- `src/main/resources/application.yml` … Google の OIDC クライアント登録
- `src/main/java/pro/kensait/spring/employee/web/SecurityConfig.java` … oauth2Login とアクセス制御
- `src/main/java/pro/kensait/spring/employee/web/EmployeeOidcUserService.java` … 認証された利用者への権限付与
