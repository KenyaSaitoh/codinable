# spring-ecs-employee — ECS（Fargate）へのデプロイ

社員管理 REST API をコンテナ化し、ECR へ登録して ECS（Fargate）で実行する基本アプリです。
Java コード、DB 初期化 SQL、Dockerfile、タスク定義、デプロイスクリプトをこのプロジェクト内にまとめています。
チャプター 2.1 で参照する AWS 準備スクリプト（講座リポジトリの `02_aws-setup`）も `aws-setup/` に同梱しています。

| ファイル | 役割 |
|---|---|
| `src/main/java` | 社員・部署を操作する REST API |
| `src/main/resources/schema.sql` / `data.sql` | 起動時の HSQLDB 初期化 |
| `Dockerfile` | bootJar を実行する Java 25 イメージ |
| `taskdef/task-definition.json` | Fargate タスク定義のテンプレート |
| `scripts/deploy_ecs.py` | タスク定義登録と ECS サービスの作成・更新 |
| `aws-setup/scripts/common.py` | boto3 のセッションとクライアントの生成（接続先の解決） |
| `aws-setup/scripts/setup_*.py` / `teardown.py` | ECR / ECS / Aurora / Cognito の準備と後片付け |
| `aws-setup/udemy-learn-spring-aws-policy.json` | 講座用 IAM ポリシー |

## 1. Codinable でローカル実行する

実行対象 `gradle:bootRun` のまま「実行」を押します。DB はタスク定義と同じく
**インメモリの HSQLDB**（`jdbc:hsqldb:mem:testdb`）で、起動時に `schema.sql` と `data.sql` を流します。
HSQLDB サーバーの起動や `setupHsqldb` は不要です。

起動したらプレビュー、またはターミナルで確認します。

```bash
curl http://localhost:8080/employees
curl http://localhost:8080/employees/1
curl http://localhost:8080/departments
```

`/employees` が `[]`（HTTP 200）を返すのは、チャプター 2.1.4「初期化の責務」で扱う状態です。
SQL 初期化の後に、組み込み DB に対する Hibernate の自動 DDL がテーブルを作り直すため、
`data.sql` で入れた行が残りません。`application.yml` の `spring.jpa.hibernate` に
`ddl-auto: none` を足して再実行すると、テーブル生成が SQL に統一され、社員の配列が返ります
（元のタスク定義にはこの設定は含まれていません）。

## 2. AWS 上で動かす（チャプター 2.1 の手順）

ここから先は **実 AWS アカウント・AWS CLI v2・Docker** が必要です。Codinable には含まれないので、
各自で用意してください。コマンドは Codinable のターミナル（このプロジェクトのフォルダ）で実行します。
アカウント ID・サブネット・セキュリティグループなどの値は、自分の環境のものに置き換えます。

### 2-1. 認証情報とポリシー

AWS の認証情報はソースコードや設定ファイルへ書かず、AWS CLI の名前付きプロファイルで管理します。

```bash
aws configure --profile udemy-learn-spring-aws
aws sts get-caller-identity --profile udemy-learn-spring-aws
```

管理者が `aws-setup/udemy-learn-spring-aws-policy.json` をカスタマー管理ポリシーとして登録し、
講座用ユーザーへ付与します。`ecsTaskExecutionRole` の作成権限は含めていません。
ロールがない場合は管理者が作成し、AWS 管理ポリシー `service-role/AmazonECSTaskExecutionRolePolicy` を付与します。
詳細は `aws-setup/README.md` を参照してください。

### 2-2. boto3 と ECR / ECS クラスター

```bash
python -m pip install -r aws-setup/requirements.txt
python aws-setup/scripts/setup_ecr.py --profile udemy-learn-spring-aws
python aws-setup/scripts/setup_ecs.py --profile udemy-learn-spring-aws
```

ECR リポジトリ `spring-ecs-employee` と ECS クラスター `udemy-spring-cluster` が作られます。
`AWS_ENDPOINT_URL` をローカルエミュレーター用に設定している場合は解除します。

### 2-3. bootJar とコンテナイメージ

実行対象で `gradle:bootJar` を選んで「実行」するか、ターミナルで `./gradlew.bat bootJar` を実行します
（`gradlew.bat` は一度「実行」を押すと Codinable がこのフォルダに用意します）。

```bash
cp build/libs/spring-ecs-employee.jar app.jar
docker build -t spring-ecs-employee .
```

ARM 環境から既定の Fargate 環境へデプロイする場合は、`docker build` に `--platform linux/amd64` を追加します。

### 2-4. ECR への push

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text \
  --profile udemy-learn-spring-aws)

aws ecr get-login-password --region ap-northeast-1 \
  --profile udemy-learn-spring-aws \
  | docker login --username AWS --password-stdin \
    "$ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com"

docker tag spring-ecs-employee:latest \
  "$ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/spring-ecs-employee:latest"
docker push \
  "$ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/spring-ecs-employee:latest"
```

### 2-5. ECS 用ネットワーク

デフォルト VPC のパブリックサブネットに配置し、パブリック IP を割り当てます。
8080 番を許可するセキュリティグループは `<自分のグローバルIP>/32` だけを許可し、`0.0.0.0/0` は使いません。

```bash
VPC_ID=$(aws ec2 describe-vpcs \
  --filters Name=is-default,Values=true \
  --query 'Vpcs[0].VpcId' --output text \
  --profile udemy-learn-spring-aws)

aws ec2 describe-subnets \
  --filters Name=vpc-id,Values="$VPC_ID" \
  --query 'Subnets[].SubnetId' --output table \
  --profile udemy-learn-spring-aws

SG_ID=$(aws ec2 create-security-group \
  --group-name spring-ecs-employee-sg \
  --description "spring-ecs-employee course access" \
  --vpc-id "$VPC_ID" --query GroupId --output text \
  --profile udemy-learn-spring-aws)

aws ec2 authorize-security-group-ingress \
  --group-id "$SG_ID" --protocol tcp --port 8080 \
  --cidr <自分のグローバルIP>/32 \
  --profile udemy-learn-spring-aws
```

### 2-6. ECS へのデプロイ

`taskdef/task-definition.json` の `<ACCOUNT_ID>` は仮の値です。`deploy_ecs.py` が引数からイメージ URI、
実行ロール ARN、ログのリージョンを埋めてから登録します。

```bash
python scripts/deploy_ecs.py \
  --profile udemy-learn-spring-aws \
  --image "$ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/spring-ecs-employee:latest" \
  --subnets <サブネットID>,<サブネットID> \
  --security-groups "$SG_ID"
```

CloudWatch Logs ロググループ、タスク定義、ECS サービスを用意し、サービスが安定するまで待機します。
タスクのヘルスチェックは `GET /employees` を使います。

### 2-7. 動作確認

```bash
TASK_ARN=$(aws ecs list-tasks --cluster udemy-spring-cluster \
  --service-name spring-ecs-employee-service \
  --query 'taskArns[0]' --output text \
  --profile udemy-learn-spring-aws)

ENI_ID=$(aws ecs describe-tasks --cluster udemy-spring-cluster \
  --tasks "$TASK_ARN" \
  --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value | [0]" \
  --output text --profile udemy-learn-spring-aws)

PUBLIC_IP=$(aws ec2 describe-network-interfaces \
  --network-interface-ids "$ENI_ID" \
  --query 'NetworkInterfaces[0].Association.PublicIp' --output text \
  --profile udemy-learn-spring-aws)

curl "http://$PUBLIC_IP:8080/employees"

# 起動しない場合はログを確認する
aws logs tail /ecs/spring-ecs-employee --follow --profile udemy-learn-spring-aws
```

### 2-8. 後片付け

Fargate タスクは実行中に課金されます。ECS サービス、クラスター、ロググループ、ECR リポジトリを削除します。

```bash
python aws-setup/scripts/teardown.py --profile udemy-learn-spring-aws
aws ec2 delete-security-group --group-id "$SG_ID" --profile udemy-learn-spring-aws
```
