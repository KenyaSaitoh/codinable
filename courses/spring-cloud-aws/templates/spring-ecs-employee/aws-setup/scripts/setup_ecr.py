# ECRリポジトリを作成するスクリプト（03_ecs章用）
# 実行例: python scripts/setup_ecr.py --profile udemy-learn-spring-aws
import argparse
import sys

from botocore.exceptions import ClientError

import common


# ECRリポジトリを作成する（作成済みならURIを取得する）
def create_repository(client, name):
    try:
        response = client.create_repository(repositoryName=name)
        print(f"リポジトリを作成した: {name}")
        return response["repository"]["repositoryUri"]
    except client.exceptions.RepositoryAlreadyExistsException:
        print(f"リポジトリは作成済みのため作成をスキップする: {name}")
        response = client.describe_repositories(repositoryNames=[name])
        return response["repositories"][0]["repositoryUri"]


def main():
    parser = argparse.ArgumentParser(description="ECRリポジトリを作成する")
    common.add_common_args(parser)
    parser.add_argument("--name", default=common.ECR_REPOSITORY_NAME,
                        help=f"リポジトリ名（既定: {common.ECR_REPOSITORY_NAME}）")
    args = parser.parse_args()

    session = common.create_session(args.profile, args.region)
    client = common.create_client(session, "ecr")

    try:
        repository_uri = create_repository(client, args.name)
    except ClientError as e:
        print(f"作成に失敗した: {e}", file=sys.stderr)
        sys.exit(1)

    print()
    print("=== 03_ecs 章で使う値（docker pushの宛先） ===")
    print(f"repositoryUri: {repository_uri}")


if __name__ == "__main__":
    main()
