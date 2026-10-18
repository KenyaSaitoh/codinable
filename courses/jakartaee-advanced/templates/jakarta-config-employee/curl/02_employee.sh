#!/usr/bin/env bash
# 部署のある社員（Alice、10001）と部署の無い社員（Victor、10014）の JSON を比べる
# 前提: 先に ▶ アプリ実行で jakarta-config-employee をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と Alice の JSON。項目は辞書順、entranceDate は "2012-04-01"
#   2つ目 HTTP/1.1 200 と Victor の JSON。JsonbConfigResolver の withNullValues(true) により
#         "departmentId":null と "departmentName":null が省略されずに出る
set -x
api=http://localhost:8080/jakarta-config-employee/api
curl -sS -i "$api/employees/10001"
echo
curl -sS -i "$api/employees/10014"
echo
