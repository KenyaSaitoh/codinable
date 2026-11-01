#!/usr/bin/env bash
# 社員一覧の SSE（GET /api/employees/stream）を curl で受信する（約 16 秒かかる）
# 前提: 先に ▶ アプリ実行で jakarta-async-employee をデプロイしておく
# 期待する結果: Content-Type: text/event-stream の応答に、data: {"employeeId":10001,...} が約 1 秒ごとに 16 件届き、
#               最後に event: complete と data: {"complete":true} が届いて接続が閉じる
set -x
curl -sS -N -i --max-time 40 http://localhost:8080/jakarta-async-employee/api/employees/stream
echo
