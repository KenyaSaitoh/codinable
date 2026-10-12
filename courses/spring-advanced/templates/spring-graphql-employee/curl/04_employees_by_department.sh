#!/usr/bin/env bash
# employeesByDepartment（部署IDをキーに条件検索）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"employeesByDepartment":[{"id":"10003","employeeName":"Carol","jobName":"CHIEF"},{"id":"10008","employeeName":"Justin","jobName":"MANAGER"}]}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ employeesByDepartment(departmentId: 2) { id employeeName jobName } }"}'
