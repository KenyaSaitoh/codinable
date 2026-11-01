#!/usr/bin/env bash
# 同じ eventId のイベントを 2 回送り、受信側が 1 回だけ記録すること（べき等性）を確かめる
# （送信側が 503 を受けて送り直した場合と同じ状況。JMSMessageID は送信のたびに変わるが、eventId は変わらない）
# 前提: 先に ▶ アプリ実行で送信側と受信側をデプロイしておく
# 期待する結果:
#   2 回とも HTTP/1.1 202
#   受信側の GET /api/events に、eventId 11111111-2222-3333-4444-555555555555 の記録は 1 件だけ
#   実行結果に 2 回目の [ EmployeeEventHandler#handle ] 処理済みのイベントです が出る
set -x
producer=http://localhost:8080/jakarta-messaging-employee/api
consumer=http://localhost:8080/jakarta-messaging-employee-consumer/api
for i in 1 2; do
  curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$producer/events" <<'JSON'
{"eventId":"11111111-2222-3333-4444-555555555555","eventType":"EMPLOYEE_DELETED","employeeId":10016,"employeeName":"Trent"}
JSON
  echo
done
sleep 2
curl -sS "$consumer/events" | grep -o '11111111-2222-3333-4444-555555555555' | wc -l
