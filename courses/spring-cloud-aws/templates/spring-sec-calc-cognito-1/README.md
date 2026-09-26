# spring-sec-calc-cognito-1 — Spring Security の OIDC 連携（Amazon Cognito）

Spring Security の `oauth2Login`（OpenID Connect）と **Amazon Cognito ユーザープール**を連携させ、
電卓アプリのログインを Cognito のマネージドログインに委譲するサンプルです。

マネージドログイン + 認可コードフローの**基本形**です。`CustomOidcUserService` は、ログインした全ユーザーへ便宜上 `ADMIN` 権限を付与します（ログイン済みなら全員が履歴を表示できる）。
権限をCognito のグループから決める発展形は、演習「spring-sec-calc-cognito-2」です。

## Codinable でできること（Cognito なし）

**Cognito は Codinable に含まれず、ログインフローの確認には実 AWS アカウントの Cognito が必要です。**
そのため、この演習の既定の実行対象は `gradle:test` です。Codinable 版では、Cognito に接続せずに
URL ごとの認可と権限の変換を確かめるテストを追加しています（講座リポジトリのサンプルには無いものです）。

| テスト | 確かめること |
|---|---|
| `SecurityConfigTest` | 未認証はログインへリダイレクト、`ADMIN` があれば履歴を表示でき、無ければ 403、計算の GET は拒否、CSRF トークンの無い POST は 403 |
| `CustomOidcUserServiceTest` | グループに関係なく `ADMIN` が付くこと、元の OIDC 権限を引き継ぐこと |

環境変数を設定せずに `gradle:bootRun` を実行すると、起動時に
`Not enough variable values available to expand 'COGNITO_USER_POOL_ID'` で失敗します。
これは「発行元（issuer-uri）を組み立てられない」段階の失敗で、チャプター 4.1.4 の表の 1 行目にあたります。

## Cognito で動かす

### ユーザープールの作成

作成方法はスクリプトとマネジメントコンソールの 2 通りです。スクリプトは演習「spring-ecs-employee」の
`aws-setup/scripts/setup_cognito.py` です。その演習のフォルダ（演習「spring-ecs-employee」を開くと作られる作業フォルダ）で実行します（`aws configure` 済みであることが前提）。

```bash
python aws-setup/scripts/setup_cognito.py   --profile udemy-learn-spring-aws   --domain <全体で一意のドメインプレフィックス>
```

コンソールで作る場合は、アプリケーションタイプを「従来のウェブアプリケーション」（クライアントシークレットが
生成されるタイプ）にし、アプリクライアントに次を設定します。

- 許可されているコールバック URL: `http://localhost:8080/login/oauth2/code/cognito`
- OpenID Connect スコープ: `openid` / `profile` / `email`
- 認可フロー: 認可コード付与（Authorization code grant）
- マネージドログイン用のドメイン（プレフィックスドメインで可）とテストユーザー

### 環境変数の設定と起動

`application.yml` はシークレットを平文で持たず、環境変数から取得します。

| 環境変数 | 内容 | 必須 |
|---|---|---|
| `COGNITO_CLIENT_ID` | アプリクライアントのクライアント ID | ○ |
| `COGNITO_CLIENT_SECRET` | アプリクライアントのクライアントシークレット | ○ |
| `COGNITO_USER_POOL_ID` | ユーザープール ID（`ap-northeast-1_` で始まる値） | ○ |
| `AWS_REGION` | リージョン（未設定時は `ap-northeast-1`） | — |

「実行」ボタンには環境変数を渡せないので、ターミナルで設定してから起動します
（`gradlew.bat` は一度「実行」を押すと Codinable がこのフォルダに用意します）。

```bash
export COGNITO_CLIENT_ID=<クライアントID>
export COGNITO_CLIENT_SECRET=<クライアントシークレット>
export COGNITO_USER_POOL_ID=<ユーザープールID>
./gradlew.bat bootRun
```

ブラウザ（またはプレビュー）で `http://localhost:8080` を開くと、Cognito のマネージドログインへ
リダイレクトされ、ログイン後は電卓（`/toInput`）が表示されます。
シークレットをファイルやリポジトリに残さないようにします。
cognito-1 と cognito-2 はどちらも 8080 を使うので同時には起動できません。

## ログアウトについて

「ログアウト」（`/processLogout`）はアプリ側のセッションを破棄するだけで、Cognito 側のセッションは残ります。
そのため直後に再ログインすると、パスワード入力なしでログインが完了する場合があります。
Cognito 側もログアウトさせるには、Cognito のログアウトエンドポイントへのリダイレクト設定が別途必要です
（このサンプルでは扱いません）。
