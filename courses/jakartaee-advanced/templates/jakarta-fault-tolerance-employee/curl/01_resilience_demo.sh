#!/usr/bin/env bash
# 社員APIの稼働中に、サーキットブレーカー・タイムアウト付きの社員一覧と、再試行付きの社員一覧を取得する
# 前提: 先に ▶ アプリ実行で jakarta-fault-tolerance-employee と社員API（api/、8095）を起動しておく
# 期待する結果:
#   1つ目 GET /api/resilience-demo は HTTP/1.1 200 と {"employees":[...16件...],"fallback":false,"message":"取得成功"}
#   2つ目 GET /api/retry-demo も 16 件と "fallback":false（1 回目で成功するので再試行しない）
#   実行結果に [ EmployeeClientService#getEmployees ] と [ EmployeeClientService#getEmployeesWithRetry ] が 1 回ずつ出る
set -x
api=http://localhost:8080/jakarta-fault-tolerance-employee/api
curl -sS -i "$api/resilience-demo"
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$api/retry-demo"
echo
