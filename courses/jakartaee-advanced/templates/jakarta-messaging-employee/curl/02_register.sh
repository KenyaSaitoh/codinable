#!/usr/bin/env bash
# 社員の登録と社員イベントの送信が、同じトランザクションで確定・取り消しされることを確かめる
# 前提: 先に ▶ アプリ実行で送信側と受信側をデプロイしておく
# 期待する結果:
#   1つ目 給与 230000 の Walter の登録は HTTP/1.1 201（社員ID 10017）。登録と送信がコミットで確定する
#   2つ目 給与 50000 の Zoe の登録は HTTP/1.1 400（給与の範囲外）。登録も送信もロールバックされる
#   3つ目 受信側の GET /api/events には、Walter の EMPLOYEE_CREATED だけがあり、Zoe のイベントは届いていない
set -x
producer=http://localhost:8080/jakarta-messaging-employee/api
consumer=http://localhost:8080/jakarta-messaging-employee-consumer/api
curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$producer/employees" <<'JSON'
{"employeeName":"Walter","departmentId":3,"jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}
JSON
echo
curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$producer/employees" <<'JSON'
{"employeeName":"Zoe","departmentId":3,"jobName":"ASSOCIATE","salary":50000,"entranceDate":"2018-04-01"}
JSON
echo
sleep 2
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$consumer/events"
echo
