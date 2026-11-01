#!/usr/bin/env bash
# 異動を登録し、SSE の一斉配信と、非同期のオブザーバーによる履歴の記録を確かめる
# 前提: 先に ▶ アプリ実行で jakarta-async-employee をデプロイしておく
# 期待する結果:
#   1つ目 異動の購読（GET /api/employees/events）を裏で 5 秒だけ開いておく
#   2つ目 POST /api/transfers（Alice を部署 1 へ）は 200 と異動後の社員。メール通知や履歴の記録は待たずに返る
#   3つ目 購読していた接続に event: transferred と、異動の内容（fromDepartmentId 3 → toDepartmentId 1）が届く
#   4つ目 GET /api/transfers に、TransferHistoryRecorder が記録した異動履歴が 1 件ある
#   5つ目 存在しない社員の異動は 404
#   実行結果（Payara Server の出力）に、TransferEventRelay#relay・TransferMailNotifier#notify・TransferHistoryRecorder#record が出る
set -x
api=http://localhost:8080/jakarta-async-employee/api
events=$(mktemp)
curl -sS -N --max-time 5 "$api/employees/events" > "$events" &
sleep 1
curl -sS -i -H 'Content-Type: application/json' -d '{"employeeId":10001,"toDepartmentId":1}' "$api/transfers"
echo
wait
cat "$events"
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$api/transfers"
echo
curl -sS -i -H 'Content-Type: application/json' -d '{"employeeId":99999,"toDepartmentId":1}' "$api/transfers"
echo
rm -f "$events"
