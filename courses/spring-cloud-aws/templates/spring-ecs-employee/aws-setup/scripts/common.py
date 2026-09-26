# 各セットアップスクリプトで共有する共通処理
import os

import boto3

# 本講座で使うリソース名（各スクリプトと teardown.py で共有する）
USER_POOL_NAME = "udemy-spring-pool"
APP_CLIENT_NAME = "udemy-spring-client"
COGNITO_ADMIN_GROUP = "ADMIN"
ECR_REPOSITORY_NAME = "spring-ecs-employee"
ECS_CLUSTER_NAME = "udemy-spring-cluster"
ECS_SERVICE_NAME = "spring-ecs-employee-service"
ECS_LOG_GROUP = "/ecs/spring-ecs-employee"
AURORA_CLUSTER_ID = "udemy-spring-aurora"
AURORA_INSTANCE_ID = "udemy-spring-aurora-writer"
AURORA_SECURITY_GROUP_NAME = "udemy-spring-aurora-sg"
AURORA_DATABASE_NAME = "coursedb"
AURORA_MASTER_USERNAME = "courseadmin"

# 既定リージョン
DEFAULT_REGION = "ap-northeast-1"


# 共通の引数（--profile / --region）をパーサーに追加する
def add_common_args(parser):
    parser.add_argument("--profile", default=None,
                        help="AWS CLIプロファイル名（例: udemy-learn-spring-aws。省略時は既定の認証情報）")
    parser.add_argument("--region", default=DEFAULT_REGION,
                        help=f"リージョン（既定: {DEFAULT_REGION}）")


# セッションを生成する
def create_session(profile, region):
    if profile:
        return boto3.Session(profile_name=profile, region_name=region)
    return boto3.Session(region_name=region)


# サービスクライアントを生成する
# 環境変数 AWS_ENDPOINT_URL（またはサービス個別の AWS_ENDPOINT_URL_<SERVICE>）が
# あれば endpoint_url に指定する = LocalStack / cognito-local に接続先を切り替える
def create_client(session, service):
    env_key = "AWS_ENDPOINT_URL_" + service.upper().replace("-", "_")
    endpoint_url = os.environ.get(env_key) or os.environ.get("AWS_ENDPOINT_URL")
    if endpoint_url:
        return session.client(service, endpoint_url=endpoint_url)
    return session.client(service)
