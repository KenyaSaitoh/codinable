#!/usr/bin/env bash
# 社員の取得を 3 回（うち 1 回は存在しない社員）呼び、カウンター employee.get.count を増やす
# 前提: 先に ▶ アプリ実行で jakarta-health-employee をデプロイしておく
# 期待する結果:
#   10001 と 10002 は HTTP 200、99999 は 404
#   最大 60 秒後、実行結果（Payara Server のログ）に書き出されるメトリクスに、
#   name=employee.get.count の値 3（employee.type=regular）が出る（存在しない社員の要求も「取得処理へ入った回数」に数える）
#   同じ出力に、実行環境が収集する http.server.request.duration や jvm.memory.used なども並ぶ
set -x
api=http://localhost:8080/jakarta-health-employee/api
curl -sS -o /dev/null -w '%{http_code}\n' "$api/employees/10001"
curl -sS -o /dev/null -w '%{http_code}\n' "$api/employees/10002"
curl -sS -o /dev/null -w '%{http_code}\n' "$api/employees/99999"
