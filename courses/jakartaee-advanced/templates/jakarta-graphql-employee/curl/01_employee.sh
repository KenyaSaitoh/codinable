#!/usr/bin/env bash
# 社員 1 件を、変数付きのクエリで取得する
# 前提: 先に ▶ アプリ実行で jakarta-graphql-employee をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と {"data":{"employee":{"id":"10001","employeeName":"Alice","department":{"id":"3","departmentName":"SALES"}}}}
#   2つ目 存在しない社員 99999 は HTTP 200 のまま、errors の message に「社員が存在しません => 99999」、data.employee は null
#   3つ目 スキーマに無いフィールド（nickname）を要求すると、リゾルバーを呼ぶ前の検証で errors になる
set -x
url=http://localhost:8080/jakarta-graphql-employee/graphql
curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$url" <<'JSON'
{"query":"query EmployeeDetail($id: ID!) { employee(id: $id) { id employeeName department { id departmentName } } }","variables":{"id":"10001"}}
JSON
echo
curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$url" <<'JSON'
{"query":"query EmployeeDetail($id: ID!) {\n  employee(id: $id) { id employeeName } }","variables":{"id":"99999"}}
JSON
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- "$url" <<'JSON'
{"query":"{ employee(id: 10001) { id nickname } }"}
JSON
echo
