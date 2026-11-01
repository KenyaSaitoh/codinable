# jakarta-faces-calc-3-config-exception — CDI・インターセプタ・設定値・業務例外

計算アプリケーションの 3 番目の版です。2 つのチャプターで同じアプリケーションを別の観点で読みます。

- チャプター 4.1「CDIによるDIとレイヤー設計」: `@Inject` によるフィールド注入、`@Produces` のプロデューサー、
  インターセプタバインディング `@Logged` と `LoggingInterceptor`
- チャプター 6.1「Jakarta Facesの入力検証・変換・例外処理」: `CalcBean` の業務検証（`FacesMessage`）、
  MicroProfile Config による設定値の注入、業務例外からエラー画面への変換、`ExceptionHandler` と `error-page`

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/calc/` の下にあります。

| ファイル | 内容 |
|---|---|
| `web/CalcBean.java` | バッキングBean。`CalcService` と `Logger` を `@Inject` で受け取る。`rejectZero`・同値チェック・業務例外の `catch` |
| `service/CalcService.java` | `@Logged` を付けた業務処理。`CalcConfig` の桁数で丸め、上限超過とゼロ除算を業務例外で通知する |
| `service/exception/LimitOverException.java`、`ZeroDivideException.java` | 業務例外（チェック例外） |
| `util/Logged.java`、`util/LoggingInterceptor.java` | インターセプタバインディングと、`@AroundInvoke` でメソッドの前後にログを出すインターセプタ |
| `util/LoggerProducer.java` | 注入先のクラス名のロガーを作るプロデューサー |
| `config/CalcConfig.java`、`src/main/resources/META-INF/microprofile-config.properties` | `@ConfigProperty` で桁数 `4` と上限 `100000` を注入する |
| `web/CalcExceptionHandler.java`、`web/CalcExceptionHandlerFactory.java` | `ViewExpiredException` を `ViewExpiredPage` へのリダイレクトに変換する |
| `src/main/webapp/WEB-INF/faces-config.xml`、`web.xml` | メッセージバンドル・`exception-handler-factory` の登録と、500 の `error-page` |
| `src/main/resources/messages.properties` | 業務検証と業務例外の文言 |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-calc-3-config-exception/CalcInputPage.xhtml` が開く
2. 2 つの数値を入れて「足し算」「引き算」「掛け算」「割り算」を押す
3. `curl/01_business_rules.sh`・`curl/02_view_expired.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

- CDI とインターセプタ（4.1）: ボタンを押すたびに、実行結果に `LoggingInterceptor` のログ
  `[ CalcService#add ] start`・`[ CalcService#add ] end 0ms` が出る。`CalcService` の中にはログを出すコードが無い
- 設定値（6.1）: `10 ÷ 3` は `3.3333`（`calc.result.scale=4`）。`1000 × 101` は丸める前の積が上限 `100000` を超えるのでエラーになる
- 業務検証（6.1）: 足し算で `0` を入れると、その項目の横に「パラメータ1の値がゼロです」。引き算で同じ値を入れると、
  ボタンの下に「同一の値同士の引き算は認められていません」（どの項目にも結び付かないグローバルなメッセージ）
- 業務例外（6.1）: 掛け算の上限超過とゼロ除算は `CalcErrorPage` に詳細情報が出る。このときの応答のステータスは 200 のまま
- `02_view_expired.sh` は、画面の状態が無いポストバックで `ViewExpiredException` を起こし、`ViewExpiredPage.xhtml` へリダイレクトされることを示す
- 入力の型・必須・範囲の検証（`abc`、空欄、`1001`）は `jakarta-faces-calc-2-validation` と同じ
