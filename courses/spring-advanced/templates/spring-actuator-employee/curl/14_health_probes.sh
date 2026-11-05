#!/usr/bin/env bash
# liveness と readiness（management.endpoint.health.probes.enabled で公開している）
# liveness: プロセスが動き続けられるか。DOWN なら再起動の対象
# readiness: 要求を受け付けられるか。DOWN なら振り分けの対象から外す（DB の状態も含めている）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: どちらも HTTP/1.1 200
#   /actuator/health/liveness  … {"status":"UP"}
#   /actuator/health/readiness … {"components":{"database":{"details":{"database":"HSQLDB","employeeCount":16},"status":"UP"},"readinessState":{"status":"UP"}},"status":"UP"}
set -x
curl -sS -i localhost:8080/actuator/health/liveness
echo
curl -sS -i localhost:8080/actuator/health/readiness
echo
