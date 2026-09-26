# ECS(Fargate)へデプロイするスクリプト（03_ecs章用）
# タスク定義テンプレート(taskdef/task-definition.json)を読み込んで登録し、
# サービスがなければ作成、あれば新しいタスク定義で更新する
#
# 実行例（このプロジェクトのディレクトリで実行）:
#   python scripts/deploy_ecs.py --profile udemy-learn-spring-aws \
#       --image <アカウントID>.dkr.ecr.ap-northeast-1.amazonaws.com/spring-ecs-employee:latest \
#       --subnets subnet-xxxx,subnet-yyyy \
#       --security-groups sg-zzzz
import argparse
import json
import os
import sys
from pathlib import Path

import boto3
from botocore.exceptions import ClientError, WaiterError

# タスク定義テンプレートの場所（このスクリプトからの相対で解決する）
TASKDEF_PATH = Path(__file__).resolve().parent.parent / "taskdef" / "task-definition.json"

DEFAULT_CLUSTER = "udemy-spring-cluster"
DEFAULT_SERVICE = "spring-ecs-employee-service"
DEFAULT_REGION = "ap-northeast-1"
LOG_GROUP = "/ecs/spring-ecs-employee"


# セッションを生成する
def create_session(profile, region):
    if profile:
        return boto3.Session(profile_name=profile, region_name=region)
    return boto3.Session(region_name=region)


# クライアントを生成する（AWS_ENDPOINT_URLがあればLocalStack等に切り替える）
def create_client(session, service):
    endpoint_url = os.environ.get("AWS_ENDPOINT_URL")
    if endpoint_url:
        return session.client(service, endpoint_url=endpoint_url)
    return session.client(service)


# awslogs用のロググループがなければ作成する
def ensure_log_group(logs_client):
    try:
        logs_client.create_log_group(logGroupName=LOG_GROUP)
        print(f"ロググループを作成した: {LOG_GROUP}")
    except logs_client.exceptions.ResourceAlreadyExistsException:
        print(f"ロググループは作成済み: {LOG_GROUP}")


# テンプレートを読み込み、引数の値でプレースホルダーを埋めてタスク定義を組み立てる
def build_task_definition(image, execution_role_arn, region):
    with open(TASKDEF_PATH, encoding="utf-8") as f:
        taskdef = json.load(f)
    taskdef["executionRoleArn"] = execution_role_arn
    container = taskdef["containerDefinitions"][0]
    container["image"] = image
    container["logConfiguration"]["options"]["awslogs-region"] = region
    return taskdef


# サービスが存在して有効(ACTIVE)かどうかを調べる
def service_is_active(ecs_client, cluster, service):
    response = ecs_client.describe_services(cluster=cluster, services=[service])
    for svc in response["services"]:
        if svc["status"] == "ACTIVE":
            return True
    return False


def main():
    parser = argparse.ArgumentParser(description="ECS(Fargate)へデプロイする")
    parser.add_argument("--profile", default=None,
                        help="AWS CLIプロファイル名（例: udemy-learn-spring-aws）")
    parser.add_argument("--region", default=DEFAULT_REGION,
                        help=f"リージョン（既定: {DEFAULT_REGION}）")
    parser.add_argument("--image", required=True,
                        help="ECRへpush済みのイメージURI（タグまで含める）")
    parser.add_argument("--subnets", required=True,
                        help="タスクを配置するサブネットID（カンマ区切り）")
    parser.add_argument("--security-groups", required=True,
                        help="タスクに付けるセキュリティグループID（カンマ区切り）")
    parser.add_argument("--cluster", default=DEFAULT_CLUSTER,
                        help=f"ECSクラスター名（既定: {DEFAULT_CLUSTER}）")
    parser.add_argument("--service", default=DEFAULT_SERVICE,
                        help=f"ECSサービス名（既定: {DEFAULT_SERVICE}）")
    parser.add_argument("--execution-role-arn", default=None,
                        help="タスク実行ロールのARN（省略時は ecsTaskExecutionRole を使う）")
    parser.add_argument("--desired-count", type=int, default=1,
                        help="タスクの希望数（既定: 1）")
    args = parser.parse_args()

    session = create_session(args.profile, args.region)
    ecs = create_client(session, "ecs")
    logs = create_client(session, "logs")
    sts = create_client(session, "sts")

    # タスク実行ロールのARNを決める（省略時はアカウントIDから組み立てる）
    execution_role_arn = args.execution_role_arn
    if not execution_role_arn:
        account_id = sts.get_caller_identity()["Account"]
        execution_role_arn = f"arn:aws:iam::{account_id}:role/ecsTaskExecutionRole"
    print(f"タスク実行ロール: {execution_role_arn}")

    try:
        # ロググループを用意する
        ensure_log_group(logs)

        # タスク定義を登録する（実行のたびに新しいリビジョンが増える）
        taskdef = build_task_definition(args.image, execution_role_arn, args.region)
        response = ecs.register_task_definition(**taskdef)
        taskdef_arn = response["taskDefinition"]["taskDefinitionArn"]
        print(f"タスク定義を登録した: {taskdef_arn}")

        # サービスを作成または更新する
        if service_is_active(ecs, args.cluster, args.service):
            ecs.update_service(
                cluster=args.cluster,
                service=args.service,
                taskDefinition=taskdef_arn,
                desiredCount=args.desired_count,
            )
            print(f"サービスを更新した: {args.service}")
        else:
            ecs.create_service(
                cluster=args.cluster,
                serviceName=args.service,
                taskDefinition=taskdef_arn,
                desiredCount=args.desired_count,
                launchType="FARGATE",
                networkConfiguration={
                    "awsvpcConfiguration": {
                        "subnets": args.subnets.split(","),
                        "securityGroups": args.security_groups.split(","),
                        # 教材ではパブリックサブネット+パブリックIPで直接アクセスする
                        "assignPublicIp": "ENABLED",
                    }
                },
            )
            print(f"サービスを作成した: {args.service}")
    except ClientError as e:
        print(f"デプロイに失敗した: {e}", file=sys.stderr)
        sys.exit(1)

    # サービスが安定する（タスクが起動しヘルスチェックが通る）まで待機する
    print("サービスが安定するまで待機する（数分かかる）...")
    try:
        waiter = ecs.get_waiter("services_stable")
        waiter.wait(cluster=args.cluster, services=[args.service])
        print("デプロイが完了した。READMEの手順でパブリックIPを調べて動作確認すること")
    except WaiterError:
        print("時間内に安定しなかった。CloudWatch Logsの "
              f"{LOG_GROUP} でコンテナのログを確認すること", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
