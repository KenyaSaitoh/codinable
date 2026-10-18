#!/usr/bin/env bash
# 社員イベントを送信し、受信側（メッセージ駆動Bean）が記録した受信履歴を確かめる
# 前提: 先に ▶ アプリ実行で送信側（jakarta-messaging-employee）と受信側（jakarta-messaging-employee-consumer）をデプロイしておく
# 期待する結果:
#   1つ目 POST /api/events は HTTP/1.1 202 と {"eventId":"..."}（ブローカーが受け付けただけで、受信側の処理の完了ではない）
#   2つ目 受信側の GET /api/events に、同じ eventId の EMPLOYEE_UPDATED（Alice）が記録されている
#   実行結果に [ EmployeeEventListener#onMessage ] ... JMSXDeliveryCount=1 と [ EmployeeEventHandler#handle ] 記録しました が出る
set -x
producer=http://localhost:8080/jakarta-messaging-employee/api
consumer=http://localhost:8080/jakarta-messaging-employee-consumer/api
curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$producer/events" <<'JSON'
{"eventType":"EMPLOYEE_UPDATED","employeeId":10001,"employeeName":"Alice"}
JSON
echo
sleep 2
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$consumer/events"
echo
