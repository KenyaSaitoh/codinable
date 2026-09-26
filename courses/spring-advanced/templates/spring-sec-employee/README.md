# spring-sec-employee — Spring Security のフォーム認証と認可

フォーム認証・URL と HTTP メソッドによる認可・CSRF 保護を備えた社員管理です。社員データと利用者情報はメモリ内にあり、再起動で初期状態に戻ります。外部サービスは不要です。

- 一般社員 `Alice / 1111` … 社員の一覧・詳細の閲覧
- 管理者 `Bob / 2222` … 閲覧に加えて登録・更新・削除
- 未認証の社員取得は 401、権限不足は 403。更新系・ログイン・ログアウトは CSRF 保護の対象（`GET /csrf` で取得したトークンをヘッダーで送る）

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8083） | 実行対象 `gradle:bootRun` で「実行」 |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押し、バックエンドを起動します。
2. ターミナルで React 画面を起動します。

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. プレビューの URL 欄に `http://localhost:5173/` を入れて開き、Alice と Bob でそれぞれログインして、できる操作の違いを確かめます。

- **ブラウザの入口は `http://localhost:5173` にそろえてください**（Vite の表示は 127.0.0.1 ですが、Cookie によるセッションと許可 Origin が localhost 前提です）。
- 画面から `/api/security/...` への要求は、Vite（`frontend/vite.config.ts`）が 8083 へ転送します。
- ターミナルから直接確かめる場合は、例えば `curl -i http://localhost:8083/employees` で 401 が返ります。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/web/SecurityConfig.java` … フィルタチェーン、認可ルール、CSRF、CORS
- `src/main/java/pro/kensait/spring/employee/web/EmployeeApi.java` … 社員 API と HTTP 応答
- `frontend/src/services/api.ts` … CSRF トークンの取得と送信
