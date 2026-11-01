#!/usr/bin/env bash
# mappings（リクエストマッピングの一覧。EmployeeApiの各メソッドが確認できる）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"contexts":{"application":{"mappings":{"dispatcherServlets":{"dispatcherServlet":[{"predicate":"{GET [/employees/{employeeId}]}",...},...]}}}}}
set -x
curl -sS -i localhost:8080/actuator/mappings
