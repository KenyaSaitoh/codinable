#!/usr/bin/env bash
# employee（主キー検索。部署までネストして一度に取得できる＝アンダーフェッチが起きない）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"employee":{"id":"10001","employeeName":"Alice","jobName":"MANAGER","salary":500000,"entranceDate":"2012-04-01","department":{"id":"3","departmentName":"SALES","location":"YOKOHAMA BRANCH"}}}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ employee(id: 10001) { id employeeName jobName salary entranceDate department { id departmentName location } } }"}'
