#!/usr/bin/env bash
# metrics（Observationから生成されたタイマー）
#   employee.get     … Observation.createNotStarted("employee.get", ...) から
#   employee.get-all … @Observed(name = "employee.get-all") から
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: どちらも HTTP/1.1 200
#   {"name":"employee.get","measurements":[{"statistic":"COUNT","value":1.0},{"statistic":"TOTAL_TIME","value":0.0123},{"statistic":"MAX","value":0.0123}],"availableTags":[{"tag":"employee.type","values":["regular"]},{"tag":"error","values":["none"]}]}
#   {"name":"employee.get-all","measurements":[{"statistic":"COUNT","value":1.0},...],...}
set -x
curl -sS -i localhost:8080/actuator/metrics/employee.get
echo
curl -sS -i localhost:8080/actuator/metrics/employee.get-all
