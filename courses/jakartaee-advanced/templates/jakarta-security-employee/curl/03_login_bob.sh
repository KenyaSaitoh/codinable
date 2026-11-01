#!/usr/bin/env bash
# Bob（ADMIN）でログインし、CSRF トークンの有無で登録の結果が変わることを確かめる
# 前提: 先に ▶ アプリ実行で jakarta-security-employee をデプロイしておく
# 期待する結果:
#   ログインの前に GET /api/csrf でトークンを取得する（ログインの POST も CsrfFilter が照合する）
#   ログインは 204（ログインの成功でトークンは破棄されるので、登録の前に取り直す）
#   CSRF トークンなしの POST /api/employees は 403（CsrfFilter が拒否する）
#   有効なトークン付きの POST は 201 と Location ヘッダー（.../api/employees/10017 など）
#   登録した社員の DELETE は 204
#   PATCH は 403（deny-uncovered-http-methods により、列挙していない HTTP メソッドは拒否される）
#   ログアウト後の GET /api/employees は 401
set -x
api=http://localhost:8080/jakarta-security-employee/api
jar=$(mktemp)
# ログインも変更要求なので、先にトークンを取得して X-CSRF-TOKEN ヘッダーに付ける
token=$(curl -sS -c "$jar" -b "$jar" "$api/csrf" | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -sS -i -c "$jar" -b "$jar" -H "X-CSRF-TOKEN: $token" --data-urlencode username=Bob --data-urlencode password=2222 "$api/login"
echo
curl -sS -i -b "$jar" -X POST -H 'Content-Type: application/json' \
  -d '{"employeeName":"Walter","departmentId":3,"jobName":"ASSOCIATE","salary":230000}' "$api/employees"
echo
token=$(curl -sS -c "$jar" -b "$jar" "$api/csrf" | sed -E 's/.*"token":"([^"]+)".*/\1/')
created=$(curl -sS -i -b "$jar" -X POST -H 'Content-Type: application/json' -H "X-CSRF-TOKEN: $token" \
  --data-binary @- "$api/employees" <<'JSON'
{"employeeName":"Walter","departmentId":3,"jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}
JSON
)
echo "$created"
id=$(echo "$created" | sed -nE 's/.*"employeeId":([0-9]+).*/\1/p')
curl -sS -i -b "$jar" -X DELETE -H "X-CSRF-TOKEN: $token" "$api/employees/$id"
echo
curl -sS -i -b "$jar" -X PATCH -H "X-CSRF-TOKEN: $token" "$api/employees/10001"
echo
curl -sS -i -b "$jar" -X POST -H "X-CSRF-TOKEN: $token" "$api/logout"
echo
curl -sS -i -b "$jar" "$api/employees"
echo
rm -f "$jar"
