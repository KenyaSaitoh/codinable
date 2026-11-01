#!/usr/bin/env bash
# getEmployees（@Observedで計装済み）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   [{"employeeId":10001,"employeeName":"Alice",...},...]
set -x
curl -sS -i localhost:8080/employees
