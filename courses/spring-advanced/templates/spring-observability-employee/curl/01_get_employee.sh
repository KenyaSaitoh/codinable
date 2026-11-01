#!/usr/bin/env bash
# getEmployee（Observation APIで計装済み。リクエストを送るとログにtraceId/spanIdが出力される）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"employeeId":10001,"employeeName":"Alice","departmentId":3,"departmentName":"SALES","jobName":"MANAGER","salary":500000,"hireDate":"2012-04-01"}
# 出力タブのアプリのログには、次の形式でtraceId/spanIdが出る
#   ... INFO [spring-observability-employee,64f3a1b2c3d4e5f6a7b8c9d0e1f2a3b4,8a21b3c4d5e6f7a8] ... EmployeeApi : [ EmployeeApi#get ]
#   ... INFO [spring-observability-employee,64f3a1b2c3d4e5f6a7b8c9d0e1f2a3b4,9b32c4d5e6f7a8b9] ... EmployeeService : [ EmployeeService#getEmployee ]
# ※同一リクエスト内はtraceIdが同じで、spanIdが処理単位で変わることを確かめる
set -x
curl -sS -i localhost:8080/employees/10001
