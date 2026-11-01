#!/usr/bin/env bash
# Actuatorでサーキットの状態を見る
# /actuator/circuitbreakers … サーキットごとの状態
# /actuator/health          … register-health-indicator: true によりサーキットの状態が反映される
# 先に ▶ アプリ実行でアプリ（8082）と呼び出し先 API（api/、8095）を起動しておく
# 期待する結果: HTTP/1.1 200
#   {"circuitBreakers":{"employeeApi":{...,"state":"CLOSED"}}}
#   {"status":"UP","components":{"circuitBreakers":{...},...}}
set -x
curl -sS -i localhost:8082/actuator/circuitbreakers
echo
curl -sS -i localhost:8082/actuator/health
