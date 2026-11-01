#!/usr/bin/env bash
# 社員イベントを Kafka のトピック employee-events へ送り、受信ループが記録した受信履歴を確かめる
# 前提: 先に ▶ アプリ実行で送信側（jakarta-kafka-employee）と受信側（jakarta-kafka-employee-consumer）をデプロイしておく
#       （Codinable 同梱の Kafka は ▶ アプリ実行のときに自動で起動する）
# 期待する結果:
#   1つ目 POST /api/events は HTTP/1.1 202 と eventId・topic（employee-events）・partition・offset
#   2つ目 同じ eventId でもう一度送ると、202 で別の offset に記録される（Kafka には 2 件とも残る）
#   3つ目 受信側の GET /api/events に、この eventId の記録は 1 件だけ（受信側が eventId で重複を見分ける）
#   実行結果に [ EmployeeEventConsumer#processAll ] partition=... offset=... key=10001 と、
#   [ EmployeeEventHandler#handle ] 記録しました、2 件目の 処理済みのイベントです が出る
set -x
producer=http://localhost:8080/jakarta-kafka-employee/api
consumer=http://localhost:8080/jakarta-kafka-employee-consumer/api
for i in 1 2; do
  curl -sS -i -H 'Content-Type: application/json' --data-binary @- "$producer/events" <<'JSON'
{"eventId":"aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee","eventType":"EMPLOYEE_UPDATED","employeeId":10001,"employeeName":"Alice"}
JSON
  echo
done
sleep 3
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$consumer/events"
echo
