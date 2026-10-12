#!/usr/bin/env bash
# createEmployee（新規作成。variablesで入力値を分離して渡すのがGraphQLの流儀）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"createEmployee":{"id":"10017","employeeName":"Walter","department":{"departmentName":"SALES"}}}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"mutation($input: EmployeeInput!) { createEmployee(input: $input) { id employeeName department { departmentName } } }","variables":{"input":{"employeeName":"Walter","departmentId":3,"jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}}}'
