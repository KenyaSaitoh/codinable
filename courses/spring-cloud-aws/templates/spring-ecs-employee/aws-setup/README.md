# aws-setup — AWS環境構築（講座リポジトリの 02_aws-setup）

本コースで主題として扱うAWSサービスは、次の4つに限定する。

| サービス | 用途 | 使用章 |
|---|---|---|
| Amazon ECR | 社員管理アプリのコンテナイメージ保管 | 2.1（spring-ecs-employee） |
| Amazon ECS（Fargate） | 社員管理アプリの実行 | 2.1（spring-ecs-employee） |
| Amazon Aurora PostgreSQL | Spring Session JDBCのセッション格納 | 3.1（spring-session-employee-jdbc） |
| Amazon Cognito | Spring SecurityとのOIDC連携 | 4.1（spring-sec-calc-cognito-1 / 2） |

IAM、STS、VPC（EC2 API）、CloudWatch Logsは、これらを動かすための補助機能として使用する。
ElastiCacheは使用しない。Redis版のSpring SessionはローカルRedisで学習する。

## 1. 前提

- AWSアカウント
- Python 3.10以上
- AWS CLI v2
- ECS へデプロイする場合はDocker

AWSの認証情報はソースコードや設定ファイルへ書かず、AWS CLIの名前付きプロファイルで管理する。
ここではプロファイル名を `udemy-learn-spring-aws` とする。

```bash
aws configure --profile udemy-learn-spring-aws
aws sts get-caller-identity --profile udemy-learn-spring-aws
```

実務ではIAM Identity Centerなどの一時認証情報を優先する。アクセスキーを使う場合は講座専用ユーザーに限定し、講座終了後に無効化または削除する。

## 2. 講座用IAMポリシー

管理者が [`udemy-learn-spring-aws-policy.json`](udemy-learn-spring-aws-policy.json) をカスタマー管理ポリシーとして登録し、講座用ユーザーへ付与する。

このポリシーは次の操作を許可する。

- ECR、ECS、Cognito、Aurora（RDS API）の作成と削除
- デフォルトVPC、サブネット、セキュリティグループの参照と操作
- ECSタスク用CloudWatch Logsロググループの操作
- 既存の `ecsTaskExecutionRole` の参照とECSタスクへの受け渡し

`ecsTaskExecutionRole` の作成権限は含めていない。ロールがない場合は管理者が作成し、AWS管理ポリシー `service-role/AmazonECSTaskExecutionRolePolicy` を付与する。

```bash
aws iam get-role --role-name ecsTaskExecutionRole \
  --profile udemy-learn-spring-aws
```

## 3. boto3の準備

このREADMEがある `aws-setup` ディレクトリで実行する（Codinable のターミナルで `cd aws-setup`）。

Codinable 同梱の Python には venv が無いため、同梱の Python へ直接インストールする。
自分で入れた Python を使う場合は、講座リポジトリと同じく `python -m venv .venv` で仮想環境を作ってよい。

```bash
python -m pip install -r requirements.txt
python -c "import boto3; print(boto3.__version__)"
```

すべてのスクリプトは `--profile` と `--region` を受け取る。リージョンの既定値は `ap-northeast-1` である。

## 4. ECRとECS（チャプター2.1）

同じコマンドを再実行しても既存リソースを再利用する。

```bash
python scripts/setup_ecr.py --profile udemy-learn-spring-aws
python scripts/setup_ecs.py --profile udemy-learn-spring-aws
```

作成される主なリソースは次のとおり。

- ECRリポジトリ: `spring-ecs-employee`
- ECSクラスター: `udemy-spring-cluster`

イメージのビルド、push、ECSサービスの作成は、1 つ上のフォルダの [`README.md`](../README.md) で行う。

## 5. Aurora PostgreSQL（チャプター3.1）

Aurora Serverless v2クラスターと `db.serverless` のライターインスタンスを作成する。Auroraは実行中に課金されるため、新規作成には `--confirm-create` が必要である。

ローカルPCから接続する演習では、現在のグローバルIPv4アドレスを `/32` で指定し、`--publicly-accessible` を付ける。`0.0.0.0/0` は指定しない。

```bash
python scripts/setup_aurora.py \
  --profile udemy-learn-spring-aws \
  --allowed-cidr 203.0.113.10/32 \
  --publicly-accessible \
  --confirm-create
```

パスワードは対話入力され、画面には表示されない。CIなど対話入力できない環境では、実行プロセスにだけ環境変数 `AWS_COURSE_DB_PASSWORD` を渡せる。値をファイルへ保存したりコミットしたりしないこと。

作成完了後、チャプター3.1の `spring-session-employee-jdbc` で使う環境変数が表示される。

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<endpoint>:5432/coursedb
SPRING_DATASOURCE_USERNAME=courseadmin
SPRING_DATASOURCE_PASSWORD=<作成時のパスワード>
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_SESSION_JDBC_INITIALIZE_SCHEMA=always
```

## 6. Cognito（チャプター4.1）

Cognitoユーザープールと、認可コードフローを使うアプリクライアントを作成する。

```bash
python scripts/setup_cognito.py \
  --profile udemy-learn-spring-aws \
  --domain <全体で一意のドメインプレフィックス>
```

コールバックURLは `http://localhost:8080/login/oauth2/code/cognito` である。実行後に表示されるUser Pool ID、Client ID、Client Secret、Issuer URIをチャプター4.1で使用する。Client Secretはコミットしないこと。

`--domain` を省略するとCognitoのマネージドログイン用ドメインは作成しない。同名のユーザープールとアプリクライアントがある場合は再利用する。

## 7. 後片付け

ECS/FargateとAuroraは課金対象である。演習後は必ず削除する。

```bash
python scripts/teardown.py --profile udemy-learn-spring-aws
```

確認プロンプトに `yes` と入力した場合だけ、次を削除する。

- ECSサービス、クラスター、CloudWatch Logsロググループ
- ECRリポジトリと格納イメージ
- Cognitoドメインとユーザープール
- Aurora DBインスタンス、クラスター、講座用セキュリティグループ

Auroraは最終スナップショットを作らずに削除する。必要なデータがある場合は、このスクリプトを使う前に手動でスナップショットを作成する。

## 8. トラブルシューティング

| 症状 | 確認事項 |
|---|---|
| `Unable to locate credentials` | `--profile` と `aws configure list --profile udemy-learn-spring-aws` |
| `AccessDenied` | 講座用ポリシーの付与と、指定中のAWSアカウント |
| Cognitoのドメイン作成に失敗 | ドメインプレフィックスを変更する |
| Auroraへ接続できない | `--publicly-accessible`、接続元IP、セキュリティグループの5432番ポート |
| ECSタスクが起動しない | `ecsTaskExecutionRole`、ECRのURI、CloudWatch Logs |
