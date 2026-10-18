#!/usr/bin/env bash
# 何をするか: 社員を登録し（POST）、応答の version を使って更新する（PUT）。もう一度古い version で更新する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと初期データに戻る）
# 期待する結果:
#   1. POST → 201、Location: …/api/employees/10017、ETag と version（登録の後に社員コードを UPDATE するので 0 とは限らない）
#   2. PUT（応答の version）→ 200 と、version が 1 増えた社員
#   3. PUT（同じ古い version）→ 409 と {"status":409,"message":"ほかの人が先に更新しました。読み込み直してください",…}
U=http://localhost:8080/jakarta-rest-employee/api
set -x
created=$(curl -s -i -X POST $U/employees -H 'Content-Type: application/json' --data-binary @- <<'JSON'
{"employeeName":"Walter","departmentId":3,"jobId":1,"salary":230000,"entranceDate":"2018-04-01"}
JSON
)
echo "$created"
id=$(echo "$created" | sed -n 's/.*"employeeId":\([0-9]*\).*/\1/p')
version=$(echo "$created" | sed -n 's/.*"version":\([0-9]*\).*/\1/p')
body="{\"employeeName\":\"Walter\",\"departmentId\":2,\"jobId\":2,\"salary\":260000,\"entranceDate\":\"2018-04-01\",\"version\":$version}"
curl -s -i -X PUT "$U/employees/$id" -H 'Content-Type: application/json' -d "$body"
echo
curl -s -i -X PUT "$U/employees/$id" -H 'Content-Type: application/json' -d "$body"
