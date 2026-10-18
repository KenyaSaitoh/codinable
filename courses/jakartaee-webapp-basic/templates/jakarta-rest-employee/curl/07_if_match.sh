#!/usr/bin/env bash
# 何をするか: 社員 10002 を取得して ETag を控え、1 回更新した後に、控えた古い ETag を If-Match に付けて更新する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと初期データに戻る）
# 期待する結果:
#   1. 1 回目の PUT（If-Match に今の ETag）→ 200 と新しい ETag
#   2. 2 回目の PUT（If-Match に古い ETag）→ 412 Precondition Failed（evaluatePreconditions。サービスまで進まない）
U=http://localhost:8080/jakarta-rest-employee/api
set -x
etag=$(curl -s -i $U/employees/10002 | sed -n 's/^ETag: *//Ip' | tr -d '\r')
version=$(echo "$etag" | tr -d '"')
body="{\"employeeName\":\"Bob\",\"departmentId\":1,\"jobId\":4,\"salary\":460000,\"entranceDate\":\"2012-04-01\",\"version\":$version}"
curl -s -i -X PUT $U/employees/10002 -H 'Content-Type: application/json' -H "If-Match: $etag" -d "$body" | grep -E '^HTTP|^ETag'
curl -s -i -X PUT $U/employees/10002 -H 'Content-Type: application/json' -H "If-Match: $etag" -d "$body" | grep -E '^HTTP|^ETag'
