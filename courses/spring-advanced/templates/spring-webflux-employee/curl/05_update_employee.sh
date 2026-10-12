#!/usr/bin/env bash
# updateEmployee（更新は200。更新対象が存在しない場合は404）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果:
#   1つ目 HTTP/1.1 200
#     {"employeeId":10002,"employeeName":"Bob","departmentId":1,"departmentName":"PLANNING","jobName":"MANAGER","salary":460000,"entranceDate":"2012-04-01"}
#   2つ目 HTTP/1.1 200（更新後の Bob を取得し直す）
#   3つ目 HTTP/1.1 404（存在しない 99999 を更新しようとする）
set -x
curl -sS -i -X PUT localhost:8089/employees/10002 -H 'Content-Type: application/json' \
  -d '{"employeeName":"Bob","departmentId":1,"departmentName":"PLANNING","jobName":"MANAGER","salary":460000,"entranceDate":"2012-04-01"}'
echo
curl -sS -i localhost:8089/employees/10002
echo
curl -sS -i -X PUT localhost:8089/employees/99999 -H 'Content-Type: application/json' \
  -d '{"employeeName":"Nobody","departmentId":3,"departmentName":"SALES","jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}'
