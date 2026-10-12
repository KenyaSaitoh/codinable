#!/usr/bin/env bash
# createEmployee（新規作成。201とLocationヘッダーが返る）→ 作った社員を取得し直す
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果: HTTP/1.1 201
#   Location: /employees/10017
#   {"employeeId":10017,"employeeName":"Walter","departmentId":3,"departmentName":"SALES","jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}
# 続く GET も HTTP/1.1 200 で同じ社員が返る（データはメモリ内。アプリを起動し直すと初期状態に戻る）
set -x
curl -sS -i -X POST localhost:8089/employees -H 'Content-Type: application/json' \
  -d '{"employeeName":"Walter","departmentId":3,"departmentName":"SALES","jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}'
echo
curl -sS -i localhost:8089/employees/10017
