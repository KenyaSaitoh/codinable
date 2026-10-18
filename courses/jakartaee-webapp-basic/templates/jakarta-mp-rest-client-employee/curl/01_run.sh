#!/usr/bin/env bash
# 何をするか: 呼び出す側のアプリケーションの POST /api/run を呼び、EmployeeClientRunner に確認用 API を順に呼び出させる
# 前提: 先に ▶ アプリ実行で、このアプリケーションと確認用 API（api/ の employee-api.war）をデプロイしておく
# 期待する結果: 200 と、Employee => … / Employees => … / Salary => … / Department 3 => [Alice, Dave, Ellen, Matilda, Peggy]
#              Location => …/employee-api/api/employees/10017 / Updated => … / Deleted => 10017 / Expected error => 404 …
set -x
curl -s -i -X POST http://localhost:8080/jakarta-mp-rest-client-employee/api/run
