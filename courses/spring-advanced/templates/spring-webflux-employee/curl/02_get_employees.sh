#!/usr/bin/env bash
# getEmployees（全件取得）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果: HTTP/1.1 200
#   [{"employeeId":10001,"employeeName":"Alice",...},{"employeeId":10002,"employeeName":"Bob",...},...]
set -x
curl -sS -i localhost:8089/employees
