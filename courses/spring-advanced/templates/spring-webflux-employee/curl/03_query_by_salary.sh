#!/usr/bin/env bash
# queryBySalary（給与範囲をキーに条件検索。Fluxのfilterオペレーター）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果: HTTP/1.1 200
#   Carol・Dave・Ellen・Oscar・Steve・Trent の6件
set -x
curl -sS -i "localhost:8089/employees/query_by_salary?lowerSalary=300000&upperSalary=400000"
