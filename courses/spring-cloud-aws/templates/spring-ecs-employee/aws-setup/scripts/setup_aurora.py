# Aurora PostgreSQL Serverless v2を作成するスクリプト（04_spring-session章用）
# 実行例:
#   python scripts/setup_aurora.py --profile udemy-learn-spring-aws \
#       --allowed-cidr 203.0.113.10/32 --publicly-accessible --confirm-create
import argparse
import getpass
import ipaddress
import os
import sys

from botocore.exceptions import ClientError, WaiterError

import common


def cidr(value):
    try:
        network = ipaddress.ip_network(value, strict=False)
    except ValueError as error:
        raise argparse.ArgumentTypeError(str(error)) from error
    if network.version != 4:
        raise argparse.ArgumentTypeError("IPv4 CIDRを指定すること")
    return str(network)


def get_default_vpc_id(ec2):
    response = ec2.describe_vpcs(
        Filters=[{"Name": "is-default", "Values": ["true"]}]
    )
    if not response["Vpcs"]:
        raise RuntimeError("デフォルトVPCが見つからない")
    return response["Vpcs"][0]["VpcId"]


def ensure_security_group(ec2, vpc_id, allowed_cidr):
    response = ec2.describe_security_groups(Filters=[
        {"Name": "vpc-id", "Values": [vpc_id]},
        {"Name": "group-name", "Values": [common.AURORA_SECURITY_GROUP_NAME]},
    ])
    if response["SecurityGroups"]:
        group_id = response["SecurityGroups"][0]["GroupId"]
        print(f"セキュリティグループは作成済み: {group_id}")
    else:
        response = ec2.create_security_group(
            GroupName=common.AURORA_SECURITY_GROUP_NAME,
            Description="Aurora access for the Spring AWS course",
            VpcId=vpc_id,
        )
        group_id = response["GroupId"]
        print(f"セキュリティグループを作成した: {group_id}")

    try:
        ec2.authorize_security_group_ingress(
            GroupId=group_id,
            IpPermissions=[{
                "IpProtocol": "tcp",
                "FromPort": 5432,
                "ToPort": 5432,
                "IpRanges": [{
                    "CidrIp": allowed_cidr,
                    "Description": "Spring AWS course client",
                }],
            }],
        )
        print(f"PostgreSQL接続を許可した: {allowed_cidr}")
    except ClientError as error:
        if error.response["Error"]["Code"] != "InvalidPermission.Duplicate":
            raise
        print(f"PostgreSQL接続許可は設定済み: {allowed_cidr}")
    return group_id


def find_cluster(rds):
    try:
        return rds.describe_db_clusters(
            DBClusterIdentifier=common.AURORA_CLUSTER_ID
        )["DBClusters"][0]
    except rds.exceptions.DBClusterNotFoundFault:
        return None


def find_instance(rds):
    try:
        return rds.describe_db_instances(
            DBInstanceIdentifier=common.AURORA_INSTANCE_ID
        )["DBInstances"][0]
    except rds.exceptions.DBInstanceNotFoundFault:
        return None


def create_cluster(rds, security_group_id, min_acu, max_acu):
    password = os.environ.get("AWS_COURSE_DB_PASSWORD")
    if not password:
        password = getpass.getpass(
            "Auroraのマスターパスワード（8文字以上、画面には表示されない）: "
        )
    if len(password) < 8:
        raise ValueError("マスターパスワードは8文字以上にすること")

    response = rds.create_db_cluster(
        DBClusterIdentifier=common.AURORA_CLUSTER_ID,
        Engine="aurora-postgresql",
        DatabaseName=common.AURORA_DATABASE_NAME,
        MasterUsername=common.AURORA_MASTER_USERNAME,
        MasterUserPassword=password,
        VpcSecurityGroupIds=[security_group_id],
        BackupRetentionPeriod=1,
        StorageEncrypted=True,
        DeletionProtection=False,
        ServerlessV2ScalingConfiguration={
            "MinCapacity": min_acu,
            "MaxCapacity": max_acu,
        },
        Tags=[{"Key": "Course", "Value": "learn-spring-aidd-aws"}],
    )
    return response["DBCluster"]


def create_instance(rds, publicly_accessible):
    response = rds.create_db_instance(
        DBInstanceIdentifier=common.AURORA_INSTANCE_ID,
        DBClusterIdentifier=common.AURORA_CLUSTER_ID,
        Engine="aurora-postgresql",
        DBInstanceClass="db.serverless",
        PubliclyAccessible=publicly_accessible,
        AutoMinorVersionUpgrade=True,
        Tags=[{"Key": "Course", "Value": "learn-spring-aidd-aws"}],
    )
    return response["DBInstance"]


def main():
    parser = argparse.ArgumentParser(
        description="Aurora PostgreSQL Serverless v2を作成する"
    )
    common.add_common_args(parser)
    parser.add_argument(
        "--allowed-cidr", required=True, type=cidr,
        help="5432番ポートへの接続を許可するIPv4 CIDR（自分のグローバルIP/32を推奨）",
    )
    parser.add_argument(
        "--publicly-accessible", action="store_true",
        help="DBインスタンスを外部接続可能にする（ローカルPCから演習する場合に指定）",
    )
    parser.add_argument(
        "--confirm-create", action="store_true",
        help="課金対象リソースの新規作成に同意する",
    )
    parser.add_argument("--min-acu", type=float, default=0.5,
                        help="最小ACU（既定: 0.5）")
    parser.add_argument("--max-acu", type=float, default=1.0,
                        help="最大ACU（既定: 1.0）")
    args = parser.parse_args()

    if args.min_acu <= 0 or args.max_acu < args.min_acu:
        parser.error("ACUは 0 < min-acu <= max-acu にすること")

    session = common.create_session(args.profile, args.region)
    ec2 = common.create_client(session, "ec2")
    rds = common.create_client(session, "rds")

    try:
        cluster = find_cluster(rds)
        instance = find_instance(rds)
        if (cluster is None or instance is None) and not args.confirm_create:
            parser.error(
                "Auroraは課金対象。内容を確認して --confirm-create を指定すること"
            )

        vpc_id = get_default_vpc_id(ec2)
        security_group_id = ensure_security_group(
            ec2, vpc_id, args.allowed_cidr
        )

        if cluster is None:
            cluster = create_cluster(
                rds, security_group_id, args.min_acu, args.max_acu
            )
            print(f"Auroraクラスターを作成中: {common.AURORA_CLUSTER_ID}")
        else:
            print(f"Auroraクラスターは作成済み: {common.AURORA_CLUSTER_ID}")

        if instance is None:
            instance = create_instance(rds, args.publicly_accessible)
            print(f"DBインスタンスを作成中: {common.AURORA_INSTANCE_ID}")
        else:
            print(f"DBインスタンスは作成済み: {common.AURORA_INSTANCE_ID}")

        print("DBインスタンスが利用可能になるまで待機する（数分かかる）...")
        rds.get_waiter("db_instance_available").wait(
            DBInstanceIdentifier=common.AURORA_INSTANCE_ID,
            WaiterConfig={"Delay": 30, "MaxAttempts": 60},
        )
        cluster = find_cluster(rds)
    except (ClientError, RuntimeError, ValueError, WaiterError) as error:
        print(f"Auroraのセットアップに失敗した: {error}", file=sys.stderr)
        sys.exit(1)

    endpoint = cluster["Endpoint"]
    port = cluster["Port"]
    print()
    print("=== 04_spring-session 章で使う環境変数 ===")
    print(f"SPRING_DATASOURCE_URL=jdbc:postgresql://{endpoint}:{port}/{common.AURORA_DATABASE_NAME}")
    print(f"SPRING_DATASOURCE_USERNAME={common.AURORA_MASTER_USERNAME}")
    print("SPRING_DATASOURCE_PASSWORD=<作成時のパスワード>")
    print("SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver")
    print("SPRING_SESSION_JDBC_INITIALIZE_SCHEMA=always")


if __name__ == "__main__":
    main()
