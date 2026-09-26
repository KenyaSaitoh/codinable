# 02章で作成したAWSリソースを削除するスクリプト
# 実行例: python scripts/teardown.py --profile udemy-learn-spring-aws
# 誤削除防止のため、実行時に確認プロンプトで「yes」の入力を求める
import argparse
import sys

from botocore.exceptions import ClientError, WaiterError

import common


def delete_ecs_resources(session):
    ecs = common.create_client(session, "ecs")
    logs = common.create_client(session, "logs")
    try:
        response = ecs.describe_services(
            cluster=common.ECS_CLUSTER_NAME,
            services=[common.ECS_SERVICE_NAME],
        )
        services = [s for s in response["services"] if s["status"] == "ACTIVE"]
        if services:
            ecs.update_service(
                cluster=common.ECS_CLUSTER_NAME,
                service=common.ECS_SERVICE_NAME,
                desiredCount=0,
            )
            ecs.delete_service(
                cluster=common.ECS_CLUSTER_NAME,
                service=common.ECS_SERVICE_NAME,
                force=True,
            )
            print(f"ECSサービスを削除中: {common.ECS_SERVICE_NAME}")
            ecs.get_waiter("services_inactive").wait(
                cluster=common.ECS_CLUSTER_NAME,
                services=[common.ECS_SERVICE_NAME],
                WaiterConfig={"Delay": 15, "MaxAttempts": 40},
            )
        ecs.delete_cluster(cluster=common.ECS_CLUSTER_NAME)
        print(f"ECSクラスターを削除した: {common.ECS_CLUSTER_NAME}")
    except ecs.exceptions.ClusterNotFoundException:
        print(f"ECSクラスターは存在しない: {common.ECS_CLUSTER_NAME}")
    except (ClientError, WaiterError) as error:
        print(f"ECSリソースの削除に失敗した: {error}", file=sys.stderr)

    try:
        logs.delete_log_group(logGroupName=common.ECS_LOG_GROUP)
        print(f"CloudWatch Logsロググループを削除した: {common.ECS_LOG_GROUP}")
    except logs.exceptions.ResourceNotFoundException:
        print(f"CloudWatch Logsロググループは存在しない: {common.ECS_LOG_GROUP}")
    except ClientError as error:
        print(f"ロググループの削除に失敗した: {error}", file=sys.stderr)


def delete_ecr_repository(session):
    client = common.create_client(session, "ecr")
    try:
        client.delete_repository(
            repositoryName=common.ECR_REPOSITORY_NAME,
            force=True,
        )
        print(f"ECRリポジトリを削除した: {common.ECR_REPOSITORY_NAME}")
    except client.exceptions.RepositoryNotFoundException:
        print(f"ECRリポジトリは存在しない: {common.ECR_REPOSITORY_NAME}")
    except ClientError as error:
        print(f"ECRリポジトリの削除に失敗した: {error}", file=sys.stderr)


def delete_aurora(session):
    rds = common.create_client(session, "rds")
    ec2 = common.create_client(session, "ec2")

    try:
        rds.delete_db_instance(
            DBInstanceIdentifier=common.AURORA_INSTANCE_ID,
            SkipFinalSnapshot=True,
            DeleteAutomatedBackups=True,
        )
        print(f"Aurora DBインスタンスを削除中: {common.AURORA_INSTANCE_ID}")
        rds.get_waiter("db_instance_deleted").wait(
            DBInstanceIdentifier=common.AURORA_INSTANCE_ID,
            WaiterConfig={"Delay": 30, "MaxAttempts": 60},
        )
    except rds.exceptions.DBInstanceNotFoundFault:
        print(f"Aurora DBインスタンスは存在しない: {common.AURORA_INSTANCE_ID}")
    except (ClientError, WaiterError) as error:
        print(f"Aurora DBインスタンスの削除に失敗した: {error}", file=sys.stderr)
        return

    try:
        rds.delete_db_cluster(
            DBClusterIdentifier=common.AURORA_CLUSTER_ID,
            SkipFinalSnapshot=True,
            DeleteAutomatedBackups=True,
        )
        print(f"Auroraクラスターを削除中: {common.AURORA_CLUSTER_ID}")
        rds.get_waiter("db_cluster_deleted").wait(
            DBClusterIdentifier=common.AURORA_CLUSTER_ID,
            WaiterConfig={"Delay": 30, "MaxAttempts": 60},
        )
    except rds.exceptions.DBClusterNotFoundFault:
        print(f"Auroraクラスターは存在しない: {common.AURORA_CLUSTER_ID}")
    except (ClientError, WaiterError) as error:
        print(f"Auroraクラスターの削除に失敗した: {error}", file=sys.stderr)
        return

    try:
        response = ec2.describe_security_groups(Filters=[
            {"Name": "group-name", "Values": [common.AURORA_SECURITY_GROUP_NAME]},
        ])
        for group in response["SecurityGroups"]:
            ec2.delete_security_group(GroupId=group["GroupId"])
            print(f"Aurora用セキュリティグループを削除した: {group['GroupId']}")
    except ClientError as error:
        print(f"セキュリティグループの削除に失敗した: {error}", file=sys.stderr)


def delete_user_pool(session):
    client = common.create_client(session, "cognito-idp")
    try:
        pools = client.list_user_pools(MaxResults=60)["UserPools"]
        targets = [p for p in pools if p["Name"] == common.USER_POOL_NAME]
        if not targets:
            print(f"Cognitoユーザープールは存在しない: {common.USER_POOL_NAME}")
            return
        for pool in targets:
            pool_id = pool["Id"]
            detail = client.describe_user_pool(UserPoolId=pool_id)["UserPool"]
            domain = detail.get("Domain")
            if domain:
                client.delete_user_pool_domain(Domain=domain, UserPoolId=pool_id)
                print(f"Cognitoドメインを削除した: {domain}")
            client.delete_user_pool(UserPoolId=pool_id)
            print(f"Cognitoユーザープールを削除した: {pool_id}")
    except ClientError as error:
        print(f"Cognitoユーザープールの削除に失敗した: {error}", file=sys.stderr)


def main():
    parser = argparse.ArgumentParser(description="講座用AWSリソースを削除する")
    common.add_common_args(parser)
    args = parser.parse_args()

    print("次のリソースを削除する。")
    print(f"  - ECSクラスター/サービス: {common.ECS_CLUSTER_NAME}")
    print(f"  - ECRリポジトリ         : {common.ECR_REPOSITORY_NAME}（イメージを含む）")
    print(f"  - Cognitoユーザープール : {common.USER_POOL_NAME}")
    print(f"  - Auroraクラスター      : {common.AURORA_CLUSTER_ID}（最終スナップショットなし）")

    answer = input("本当に削除してよいか？ (yes/no): ")
    if answer.strip() != "yes":
        print("キャンセルした（何も削除していない）")
        return

    session = common.create_session(args.profile, args.region)
    delete_ecs_resources(session)
    delete_ecr_repository(session)
    delete_user_pool(session)
    delete_aurora(session)

    print()
    print("後片付けを終了した。マネジメントコンソールでも残存リソースを確認すること")


if __name__ == "__main__":
    main()
