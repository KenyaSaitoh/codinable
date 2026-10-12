#!/usr/bin/env bash
# 事前準備：メトリクスを発生させるため、社員APIを呼び出しておく
# （05_employee_get_count.sh の前後で実行すると、カウンターが増えるのを確かめられる）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: どちらも HTTP/1.1 200
set -x
curl -sS -i localhost:8080/employees/10001
echo
curl -sS -i localhost:8080/employees
