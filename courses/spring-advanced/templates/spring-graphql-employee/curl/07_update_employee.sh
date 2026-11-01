#!/usr/bin/env bash
# updateEmployee（更新）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"updateEmployee":{"id":"10002","employeeName":"Bob","salary":460000}}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"mutation($id: ID!, $input: EmployeeInput!) { updateEmployee(id: $id, input: $input) { id employeeName salary } }","variables":{"id":10002,"input":{"employeeName":"Bob","departmentId":1,"jobName":"MANAGER","salary":460000,"entranceDate":"2012-04-01"}}}'
