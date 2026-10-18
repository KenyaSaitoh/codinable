#!/usr/bin/env bash
# 何をするか: 検索条件（@BeanParam の EmployeeSearchParam）で絞り込む。月給の範囲を逆にした要求も送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   departmentId=3&salaryFrom=300000 → Alice・Dave・Ellen の 3 件（totalElements 3）
#   keyword=al → 氏名に「al」を含む Mallory だけ（LIKE は大文字と小文字を区別するので Alice は含まれない）
#   salaryFrom=500000&salaryTo=100000 → 400 と {"status":400,"message":"月給の範囲が正しくありません",…}（RangeExceptionMapper）
set -x
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-rest-employee/api/employees?departmentId=3&salaryFrom=300000"
echo
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-rest-employee/api/employees?keyword=al"
echo
curl -sS -i "http://localhost:8080/jakarta-rest-employee/api/employees?salaryFrom=500000&salaryTo=100000"
