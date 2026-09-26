# Cognitoユーザープールとアプリクライアントを作成するスクリプト（05_oidc-cognito章用）
# 実行例: python scripts/setup_cognito.py --profile udemy-learn-spring-aws
import argparse
import sys

from botocore.exceptions import ClientError

import common

# Spring Securityの既定のコールバックURL（registrationId=cognito）
CALLBACK_URL = "http://localhost:8080/login/oauth2/code/cognito"
LOGOUT_URL = "http://localhost:8080/"


# ユーザープールを作成する（同名プールがあれば再利用する）
def ensure_user_pool(client):
    paginator = client.get_paginator("list_user_pools")
    for page in paginator.paginate(PaginationConfig={"PageSize": 60}):
        for pool in page["UserPools"]:
            if pool["Name"] == common.USER_POOL_NAME:
                print(f"ユーザープールは作成済み: {common.USER_POOL_NAME}")
                return pool["Id"]
    response = client.create_user_pool(
        PoolName=common.USER_POOL_NAME,
        AutoVerifiedAttributes=["email"],
    )
    print(f"ユーザープールを作成した: {common.USER_POOL_NAME}")
    return response["UserPool"]["Id"]


# アプリクライアントを作成する（認可コードフロー + クライアントシークレット付き）
def ensure_app_client(client, pool_id):
    response = client.list_user_pool_clients(UserPoolId=pool_id, MaxResults=60)
    for app_client in response["UserPoolClients"]:
        if app_client["ClientName"] == common.APP_CLIENT_NAME:
            print(f"アプリクライアントは作成済み: {common.APP_CLIENT_NAME}")
            return client.describe_user_pool_client(
                UserPoolId=pool_id,
                ClientId=app_client["ClientId"],
            )["UserPoolClient"]
    response = client.create_user_pool_client(
        UserPoolId=pool_id,
        ClientName=common.APP_CLIENT_NAME,
        GenerateSecret=True,
        AllowedOAuthFlows=["code"],
        AllowedOAuthScopes=["openid", "profile", "email"],
        AllowedOAuthFlowsUserPoolClient=True,
        CallbackURLs=[CALLBACK_URL],
        LogoutURLs=[LOGOUT_URL],
        SupportedIdentityProviders=["COGNITO"],
    )
    print(f"アプリクライアントを作成した: {common.APP_CLIENT_NAME}")
    return response["UserPoolClient"]


# Hosted UI用のドメインを作成する（--domain 指定時のみ）
def ensure_domain(client, pool_id, domain):
    pool = client.describe_user_pool(UserPoolId=pool_id)["UserPool"]
    if pool.get("Domain") == domain:
        print(f"ドメインは作成済み: {domain}")
        return
    client.create_user_pool_domain(Domain=domain, UserPoolId=pool_id)
    print(f"ドメインを作成した: https://{domain}.auth.{client.meta.region_name}.amazoncognito.com")


def ensure_admin_group(client, pool_id):
    try:
        client.get_group(
            GroupName=common.COGNITO_ADMIN_GROUP,
            UserPoolId=pool_id,
        )
        print(f"Cognitoグループは作成済み: {common.COGNITO_ADMIN_GROUP}")
    except client.exceptions.ResourceNotFoundException:
        client.create_group(
            GroupName=common.COGNITO_ADMIN_GROUP,
            UserPoolId=pool_id,
            Description="Administrators for the Spring AWS course",
        )
        print(f"Cognitoグループを作成した: {common.COGNITO_ADMIN_GROUP}")


def main():
    parser = argparse.ArgumentParser(
        description="Cognitoユーザープールとアプリクライアントを作成する")
    common.add_common_args(parser)
    parser.add_argument("--domain", default=None,
                        help="Hosted UI用ドメインのプレフィックス（省略時は作成しない）")
    args = parser.parse_args()

    session = common.create_session(args.profile, args.region)
    client = common.create_client(session, "cognito-idp")

    try:
        pool_id = ensure_user_pool(client)
        app_client = ensure_app_client(client, pool_id)
        ensure_admin_group(client, pool_id)

        if args.domain:
            ensure_domain(client, pool_id, args.domain)
    except ClientError as e:
        print(f"作成に失敗した: {e}", file=sys.stderr)
        sys.exit(1)

    # application.yml（05章）で使う値を表示する
    print()
    print("=== 05_oidc-cognito 章で使う値（Client Secretはコミット禁止） ===")
    print(f"User Pool ID  : {pool_id}")
    print(f"Client ID     : {app_client['ClientId']}")
    print(f"Client Secret : {app_client['ClientSecret']}")
    print(f"Issuer URI    : https://cognito-idp.{args.region}.amazonaws.com/{pool_id}")
    print(f"Callback URL  : {CALLBACK_URL}")


if __name__ == "__main__":
    main()
