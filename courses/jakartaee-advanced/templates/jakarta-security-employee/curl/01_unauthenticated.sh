#!/usr/bin/env bash
# ログインせずに社員APIを呼ぶ
# 前提: 先に ▶ アプリ実行で jakarta-security-employee をデプロイしておく
# 期待する結果:
#   1つ目 GET /api/employees は HTTP/1.1 401（EmployeeAuthenticationMechanism の responseUnauthorized）
#   2つ目 POST /api/employees も 401（コンテナの判定が CsrfFilter より先に行われる）
#   3つ目 GET /api/csrf は 200（制約を書いていない入口なので、未認証でも届く）
set -x
api=http://localhost:8080/jakarta-security-employee/api
curl -sS -i "$api/employees"
echo
curl -sS -i -X POST -H 'Content-Type: application/json' -d '{}' "$api/employees"
echo
curl -sS -i "$api/csrf"
echo
