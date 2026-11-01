#!/usr/bin/env bash
# env（環境変数・システムプロパティ・application.ymlの設定値一覧）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"activeProfiles":[],"propertySources":[{"name":"systemProperties",...},{"name":"Config resource 'class path resource [application.yml]'",...}]}
set -x
curl -sS -i localhost:8080/actuator/env
