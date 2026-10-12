#!/usr/bin/env bash
# retryDemo（正常系。呼び出し先 API の稼働中は1回目の試行で成功する）
# 先に ▶ アプリ実行でアプリ（8081）と呼び出し先 API（api/、8095）を起動しておく
# 期待する結果: HTTP/1.1 200
#   {"employees":[...16件...],"fallback":false,"message":"取得成功"}
# 出力のログ: [ EmployeeClientService#getEmployees ] 試行 1 回目
set -x
curl -sS -i localhost:8081/retry-demo
