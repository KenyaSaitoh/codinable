#!/usr/bin/env bash
# Mutation で社員を登録・更新・削除する
# 前提: 先に ▶ アプリ実行で jakarta-graphql-employee をデプロイしておく
# 期待する結果:
#   1つ目 createEmployee は採番された id（初期社員の次は "10017"）の Walter を返す
#   2つ目 存在しない部署 9 での登録は errors に「部署が存在しません => 9」
#   3つ目 updateEmployee で給与が 250000 になる
#   4つ目 deleteEmployee は削除した id を返す
#   5つ目 存在しない日付 2026-02-30 は、スカラーの変換に失敗してリゾルバーに届く前に errors になる
set -x
url=http://localhost:8080/jakarta-graphql-employee/graphql
created=$(curl -sS -H 'Content-Type: application/json' --data-binary @- "$url" <<'JSON'
{"query":"mutation Create($input: EmployeeInput!) { createEmployee(input: $input) { id employeeName salary department { departmentName } } }","variables":{"input":{"employeeName":"Walter","departmentId":"3","jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}}}
JSON
)
echo "$created"
id=$(echo "$created" | sed -E 's/.*"id":"([0-9]+)".*/\1/')
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- "$url" <<'JSON'
{"query":"mutation { createEmployee(input: {employeeName: \"Zoe\", departmentId: 9, salary: 1}) { id } }"}
JSON
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- "$url" <<JSON
{"query":"mutation Update(\$id: ID!, \$input: EmployeeInput!) { updateEmployee(id: \$id, input: \$input) { id salary } }","variables":{"id":"$id","input":{"employeeName":"Walter","departmentId":"3","salary":250000}}}
JSON
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- "$url" <<JSON
{"query":"mutation { deleteEmployee(id: $id) }"}
JSON
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- "$url" <<'JSON'
{"query":"mutation { createEmployee(input: {employeeName: \"Zoe\", departmentId: 1, salary: 1, entranceDate: \"2026-02-30\"}) { id } }"}
JSON
echo
