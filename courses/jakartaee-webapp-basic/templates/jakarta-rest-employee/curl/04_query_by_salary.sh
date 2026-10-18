#!/usr/bin/env bash
# 何をするか: 月給が下限以上の社員を取得する（/employees/query_by_salary は /employees/{employeeId} より優先される）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200 と、月給 450,000 以上の Alice・Bob・Ivan・Justin の配列
set -x
curl -s "http://localhost:8080/jakarta-rest-employee/api/employees/query_by_salary?lowerSalary=450000"
