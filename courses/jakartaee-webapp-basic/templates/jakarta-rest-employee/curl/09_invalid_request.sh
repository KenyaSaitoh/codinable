#!/usr/bin/env bash
# 何をするか: 不正な本文で登録する（制約違反・存在しない部署・壊れた JSON）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   1. 氏名が空・月給が負 → 400 と errors（"employeeName: …"、"salary: …"。ConstraintViolationExceptionMapper）
#   2. departmentId=9 → 400 と {"message":"指定した部署が存在しません"}（InvalidRequestException）
#   3. 壊れた JSON → 400（本文を EmployeeRequest に変換する段階で失敗する。ProcessingExceptionMapper）
U=http://localhost:8080/jakarta-rest-employee/api
set -x
curl -sS -i -X POST $U/employees -H 'Content-Type: application/json' \
  -d '{"employeeName":"","departmentId":3,"jobId":1,"salary":-1,"entranceDate":"2018-04-01"}'
echo
curl -sS -i -X POST $U/employees -H 'Content-Type: application/json' \
  -d '{"employeeName":"Walter","departmentId":9,"jobId":1,"salary":230000,"entranceDate":"2018-04-01"}'
echo
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -X POST $U/employees -H 'Content-Type: application/json' -d '{"employeeName":'
