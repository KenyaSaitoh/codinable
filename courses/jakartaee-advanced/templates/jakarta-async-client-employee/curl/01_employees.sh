#!/usr/bin/env bash
# client/ のクライアントが呼ぶ社員APIを、curl で直接確かめる
# 前提: 先に ▶ アプリ実行で jakarta-async-client-employee をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と Alice（10001）の JSON
#   2つ目 HTTP/1.1 200 と部署3（SALES）の社員 5 人の JSON 配列
#   3つ目 HTTP/1.1 404（存在しない社員）
set -x
api=http://localhost:8080/jakarta-async-client-employee/api
curl -sS -i "$api/employees/10001"
echo
curl -sS -i "$api/employees/query_by_department?departmentId=3"
echo
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' "$api/employees/99999"
