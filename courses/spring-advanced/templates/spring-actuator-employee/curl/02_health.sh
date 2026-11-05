#!/usr/bin/env bash
# health（アプリケーションの稼働状態。ECSのヘルスチェック対象）
# カスタムのDatabaseHealthIndicatorが"database"コンポーネントとして表示される
# （講座の「HSQLDBサーバーを停止して503でDOWNを返す」手順は、インメモリ DB の Codinable では行えない）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"components":{"database":{"details":{"database":"HSQLDB","employeeCount":16},"status":"UP"},"db":{...},"diskSpace":{...},"livenessState":{"status":"UP"},"ping":{"status":"UP"},"readinessState":{"status":"UP"},...},"groups":["liveness","readiness"],"status":"UP"}
set -x
curl -sS -i localhost:8080/actuator/health
