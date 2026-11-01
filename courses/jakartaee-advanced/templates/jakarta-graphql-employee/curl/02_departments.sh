#!/usr/bin/env bash
# 部署とその所属社員をまとめて取得する（Department.employees は @Source List<Department> のバッチで解決する）
# 前提: 先に ▶ アプリ実行で jakarta-graphql-employee をデプロイしておく
# 期待する結果:
#   部署 4 件（PLANNING・HR・SALES・PRODUCT）と、それぞれの所属社員の id と employeeName（Victor はどの部署にも出ない）
#   実行結果（Payara Server の出力）に [ EmployeeService#employeesByDepartments ] departments=4 が 1 回だけ出る
set -x
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -H 'Content-Type: application/json' --data-binary @- http://localhost:8080/jakarta-graphql-employee/graphql <<'JSON'
{"query":"query DepartmentsWithEmployees { departments { id departmentName employees { id employeeName } } }"}
JSON
echo
