#!/usr/bin/env bash
# 何をするか: 呼び出し先の確認用 API を直接呼ぶ（Rest Client が送っている要求と同じもの）
# 前提: 先に ▶ アプリ実行でデプロイしておく
# 期待する結果: 社員 10001 の JSON、給与 300000〜400000 の社員の配列、存在しない 99999 の 404
set -x
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' http://localhost:8080/employee-api/api/employees/10001
echo
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/employee-api/api/employees/query_by_salary?lowerSalary=300000&upperSalary=400000"
echo
curl -sS -i http://localhost:8080/employee-api/api/employees/99999
