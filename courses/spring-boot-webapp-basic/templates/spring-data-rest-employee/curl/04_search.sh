#!/usr/bin/env bash
# Repositoryの検索メソッドを/search配下から呼ぶ
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果: すべて HTTP/1.1 200
#   1つ目は公開されている検索メソッドへの _links（by-salary・by-department・by-job など）
#   2つ目以降はそれぞれの条件に合う社員
set -x
curl -sS -i http://localhost:8080/api/employees/search
echo
curl -sS -i 'http://localhost:8080/api/employees/search/by-salary?lowerSalary=500000&sort=employeeId,asc'
echo
curl -sS -i 'http://localhost:8080/api/employees/search/by-department?departmentId=2&sort=employeeId,asc'
echo
curl -sS -i 'http://localhost:8080/api/employees/search/by-job?jobId=1&sort=employeeId,asc'
