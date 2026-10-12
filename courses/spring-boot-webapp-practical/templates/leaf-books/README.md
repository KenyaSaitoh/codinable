# Leaf Books（SPA 版: REST API + React）

React の画面（`frontend/`）と、JSON を返す Spring Boot の REST API
（このフォルダ直下、ポート 8080）で動く書店アプリです。
ログインと顧客登録では、外部 API の Customer Hub（ポート 8081）を呼びます。

## この雛形を使う演習

| 演習 | 主に読むファイル |
|---|---|
| SPA版のバックエンドをレイヤで読む（2.1） | `src/main/java/pro/kensait/leafbooks/` の `api/` `service/` `repository/` `entity/` `external/`（例: `api/OrderController.java`） |
| ReactのSPAで状態の移り変わりを追う（5.1） | `frontend/src/App.tsx`、`frontend/src/contexts/CartContext.tsx`、`frontend/src/pages/OrderConfirmPage.tsx` |
| REST APIのJWT認証とエラー応答を確かめる（5.1） | `api/AuthController.java`、`security/JwtAuthenticationFilter.java`、`api/OrderController.java`、`api/GlobalExceptionHandler.java` |

## Codinable での動かし方

動かすプロセスは 3 つで、ツールバーの **▶ アプリ実行** を押すとまとめて起動します
（`codinable.services.json` に書いてあります）。

| プロセス | ポート | 出力の目印 |
|---|---|---|
| REST API（このフォルダ直下） | 8080 | なし |
| Customer Hub（`customer-hub/`、ログイン・顧客登録で呼ぶ外部 API） | 8081 | `[customer-hub]` |
| React の開発サーバー（`frontend/`、`npm run dev`） | 5173 | `[frontend]` |

1. **▶ アプリ実行** を押す。初回は `frontend/` の `npm install` が自動で走るので少し時間がかかる
2. React の画面が待ち受けを始めると、プレビューが `http://localhost:5173` に移る。
   `/api` への要求は Vite が `http://localhost:8080` へ中継する（`frontend/vite.config.ts`）
3. ログイン画面でメールアドレス `alice@gmail.com`、パスワード `password` を入れ、
   書籍の選択 → カート → 注文確認 → 注文 → 注文履歴と進める
   （bob / carol / dave / ellen も同じパスワード）

Customer Hub と React の画面は「停止」では止まりません。止める・起動し直すときは出力タブの行の右端のボタンを使います。

REST API は `/api/auth/login` 以外は認証が必要です。
ブラウザで `http://localhost:8080/api/books` を直接開くと 403 になるのはそのためです。

## テスト

`src/test/java/pro/kensait/leafbooks/` のテストクラス（Service の Mockito テスト、Repository の `@DataJpaTest`、
`external/CustomerApiClientTest.java` など）を開いて、エディタの右上の ▶ 実行で流してください。
例: `repository/StockRepositoryTest.java`、`service/order/OrderServiceTest.java`。

## curl で API を確かめる

JWT は HttpOnly Cookie（`token`）で、更新系の要求には CSRF トークン
（Cookie `XSRF-TOKEN` の値をヘッダー `X-XSRF-TOKEN` に）が要ります。

▶ アプリ実行で起動したまま、`curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で送ります。

| スクリプト | 確かめること |
|---|---|
| `curl/01_before_login.sh` | ログイン前の `/api/books` は 403（XSRF-TOKEN の Cookie だけ受け取る） |
| `curl/02_login.sh` | XSRF-TOKEN を付けて alice でログインし、JWT の Cookie で `/api/books` と `/api/orders/history` を取得する |

Cookie は一時ファイル（cookie jar）に保存して次の要求へ持ち回り、終わったら消しています。

## DB

HSQLDB のインメモリで動かしています。起動のたびに `sql/hsqldb/` の
`2_BOOKSTORE_DDL.sql` と `3_BOOKSTORE_DML.sql` から作り直されるので、
注文や在庫の変更は停止すると元に戻ります。
元の講座のように HSQLDB サーバーを起動する必要はありません。

## 構成

```
src/main/java/pro/kensait/leafbooks/
├── api/          REST Controller（auth / books / orders / images）と DTO
├── security/     JWT の発行・検証（JwtUtil・JwtAuthenticationFilter）と Spring Security の設定
├── service/      業務処理（OrderService・BookService・DeliveryFeeService など）
├── repository/   Spring Data JPA
├── entity/       JPA Entity
└── external/     Customer Hub を呼ぶ CustomerApiClient
src/main/resources/static/images/covers/   書籍の表紙画像（/api/images/covers/{id} で配信）
frontend/         React + TypeScript + Vite の画面（pages / components / contexts / services）
customer-hub/     外部 API 役の Customer Hub（別の Gradle プロジェクト）
sql/hsqldb/       起動時に流す DDL / DML
```
