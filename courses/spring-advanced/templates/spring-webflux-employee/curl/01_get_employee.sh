#!/usr/bin/env bash
# getEmployee（主キー検索。存在する場合は200、存在しない場合は404。switchIfEmptyによる切り替え）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果:
#   1つ目 HTTP/1.1 200
#     {"employeeId":10001,"employeeName":"Alice","departmentId":3,"departmentName":"SALES","jobName":"MANAGER","salary":500000,"entranceDate":"2012-04-01"}
#   2つ目 HTTP/1.1 404
set -x
curl -sS -i localhost:8089/employees/10001
echo
curl -sS -i localhost:8089/employees/99999
