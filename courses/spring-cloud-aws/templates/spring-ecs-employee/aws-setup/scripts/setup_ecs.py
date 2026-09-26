# ECSクラスターを作成するスクリプト（03_ecs章用）
# 実行例: python scripts/setup_ecs.py --profile udemy-learn-spring-aws
import argparse
import sys

from botocore.exceptions import ClientError

import common


# ECSクラスターを作成する（同名クラスターが既にあればそのまま返る）
def create_cluster(client):
    response = client.create_cluster(clusterName=common.ECS_CLUSTER_NAME)
    return response["cluster"]


def main():
    parser = argparse.ArgumentParser(description="ECSクラスターを作成する")
    common.add_common_args(parser)
    args = parser.parse_args()

    session = common.create_session(args.profile, args.region)
    client = common.create_client(session, "ecs")

    try:
        cluster = create_cluster(client)
    except ClientError as e:
        print(f"作成に失敗した: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"クラスターを作成した: {cluster['clusterName']}")
    print()
    print("=== 03_ecs 章で使う値 ===")
    print(f"clusterArn: {cluster['clusterArn']}")
    print(f"status    : {cluster['status']}")


if __name__ == "__main__":
    main()
