# spring-sec-employee — Spring Security のフォーム認証と認可

フォーム認証・URL と HTTP メソッドによる認可・CSRF 保護を備えた社員管理です。社員データと利用者情報はメモリ内にあり、再起動で初期状態に戻ります。外部サービスは不要です。

- 一般社員 `Alice / 1111` … 社員の一覧・詳細の閲覧
- 管理者 `Bob / 2222` … 閲覧に加えて登録・更新・削除
- 未認証の社員取得は 401、権限不足は 403。更新系・ログイン・ログアウトは CSRF 保護の対象（`GET /csrf` で取得したトークンをヘッダーで送る）

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8083） | ▶ アプリ実行 |
| `frontend/` | 専用の React 画面（Vite、5173） | ▶ アプリ実行で一緒に起動 |

## 動かし方

1. ▶ アプリ実行を押します。バックエンドと React 画面（`frontend/`）が一緒に起動します。`npm install` も必要なときに自動で走ります。
2. React 画面の用意ができると、プレビューに `http://localhost:5173/` が開きます。Alice と Bob でそれぞれログインして、できる操作の違いを確かめます。

- **Webブラウザの入口は `http://localhost:5173` にそろえてください**（Cookie は開いたホスト名ごとに保持されるので、localhost と 127.0.0.1 を混ぜるとセッションが別々になります）。
- 画面から `/api/security/...` への要求は、Vite（`frontend/vite.config.ts`）が 8083 へ転送します。
- React 画面の出力は実行結果タブに `[frontend] ` を付けて出ます。「停止」ではバックエンドだけが止まり、React 画面は出力タブの行の右端にある「frontend停止」「frontend起動」で個別に操作します。
- curl で直接確かめる場合は、`curl/01_unauthenticated.sh` を開いてエディタの右上の ▶ 実行で送ると、`GET http://localhost:8083/employees` に 401 が返ります。
- `curl/02_session_cookie.sh` を開いて ▶ 実行すると、セッションの Cookie の属性と、ログインでセッション ID が変わることを確かめられます。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/web/SecurityConfig.java` … フィルタチェーン、認可ルール、CSRF、セッション固定化攻撃の対策、BCrypt によるパスワードのハッシュ化
- `src/main/resources/application.yml` … セッションの Cookie の属性（`server.servlet.session.cookie.*`）
- `src/main/java/pro/kensait/spring/employee/web/EmployeeApi.java` … 社員 API と HTTP 応答
- `frontend/src/services/api.ts` … CSRF トークンの取得と送信

## 結果のどこを見るか

- `curl/02_session_cookie.sh` の 1 つ目 … `Set-Cookie: SECURITY_SESSION=...; Path=/; HttpOnly; SameSite=Lax`。`application.yml` の `http-only` と `same-site` が効いています（`secure` は `false` なので `Secure` は付きません。本番では `true` にします）
- 2 つ目のログインの応答 … `Set-Cookie` で別の `SECURITY_SESSION` が届きます。4 つ目でログイン前の ID を送ると 401 になり、ログイン前の ID を知られていても使えないことが分かります（`sessionFixation` の `changeSessionId`）
- 実行結果の起動時のログ `Alice のパスワードの保存形式 => $2a$10$...` … BCrypt で保存される形式です。実際の値の例は次のとおりで、ソルトは起動のたびに変わります

  ```text
  $2a$10$epNwlIwcgvcTjNrkLkd7MuNB0zcB.rOt65a7XSa4rlP11Yd68BEVC
  ```

  `$2a` はアルゴリズムの版、`$10` はコスト（2 の 10 乗回の繰り返し）、続く 22 文字がソルト、残りの 31 文字がハッシュ値です。ソルトとコストが値の中にあるので、別の列に保存する必要はありません
