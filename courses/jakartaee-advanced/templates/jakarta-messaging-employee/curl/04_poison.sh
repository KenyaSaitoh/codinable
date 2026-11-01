#!/usr/bin/env bash
# JSON として読めない本文（ポイズンメッセージ）を送り、再配送とデッドレターキューへの移動を確かめる
# 前提: 先に ▶ アプリ実行で送信側と受信側をデプロイしておく
# 期待する結果:
#   POST /api/events/raw は HTTP/1.1 202
#   実行結果に [ EmployeeEventListener#onMessage ] が JMSXDeliveryCount=1、続けて JMSXDeliveryCount=2 で出る
#   （onMessage が実行時例外を送出してロールバックされ、再配送されている）
#   Payara の場合（OpenMQ）、再配送の回数の上限を超えると MQJMSRA_MR1101: ... marked for routing to the DMQ が出て、
#   メッセージはデッドレターキュー（mq.sys.dmq）へ移され、それ以上は配送されない（この演習の環境では 2 回目の配送の失敗で移った）
#   受信側の GET /api/events には、このメッセージの記録は無い
set -x
producer=http://localhost:8080/jakarta-messaging-employee/api
consumer=http://localhost:8080/jakarta-messaging-employee-consumer/api
curl -sS -i -H 'Content-Type: text/plain' --data-binary '{broken' "$producer/events/raw"
echo
sleep 5
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$consumer/events"
echo
