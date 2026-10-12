#!/usr/bin/env bash
# beans（DIコンテナに登録された全Beanの一覧）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"contexts":{"application":{"beans":{"employeeService":{"type":"pro.kensait.spring.employee.rest.service.EmployeeService",...},...}}}}
set -x
curl -sS -i localhost:8080/actuator/beans
