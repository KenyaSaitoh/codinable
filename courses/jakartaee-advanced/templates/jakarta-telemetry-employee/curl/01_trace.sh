#!/usr/bin/env bash
# 社員の取得を呼び、トレースのスパンを実行結果（Payara Server のログ）で確かめる
# 前提: 先に ▶ アプリ実行で jakarta-telemetry-employee をデプロイしておく
# 期待する結果:
#   1つ目 GET /api/employees/10001 は HTTP 200。traceparent ヘッダーを付けて送るので、トレースIDはヘッダーの値を引き継ぐ
#   2つ目 GET /api/employees は 16 件
#   実行結果に次が出る（スパンはまとめて書き出されるので、数秒遅れることがある）
#     [ EmployeeService#getEmployee ] traceId=4bf92f3577b34da6a3ce929d0e0e4736 spanId=...
#     'employee.get' : 4bf92f3577b34da6a3ce929d0e0e4736 ... employee.id=10001, employee.type=regular
#     'GET /jakarta-telemetry-employee/api/employees/{employeeId}' : 同じトレースID（employee.get の親の SERVER のスパン）
#     'employee.get-all' ... employee.count=16
set -x
api=http://localhost:8080/jakarta-telemetry-employee/api
curl -sS -i -H 'traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01' "$api/employees/10001"
echo
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' "$api/employees"
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' "$api/employees/99999"
