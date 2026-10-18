#!/usr/bin/env bash
# Alice（BASIC）でログインし、社員の参照と登録を試す
# Cookie（SECURITY_SESSION）は一時ファイル（cookie jar）に保存して、次の要求へ持ち回る
# 前提: 先に ▶ アプリ実行で jakarta-security-employee をデプロイしておく
# 期待する結果:
#   ログインの前に GET /api/csrf でトークンを取得する（ログインの POST も CsrfFilter が照合する）
#   ログインの誤ったパスワードは 401、正しいパスワード（1111）は 204 と Set-Cookie: SECURITY_SESSION=...
#   GET /api/session は {"roles":["BASIC"],"username":"Alice"}
#   GET /api/employees/10001 は 200
#   有効な CSRF トークンを付けた POST /api/employees は 403（ADMIN ロールを持たない）
set -x
api=http://localhost:8080/jakarta-security-employee/api
jar=$(mktemp)
# ログインも変更要求なので、先にトークンを取得して X-CSRF-TOKEN ヘッダーに付ける
token=$(curl -sS -c "$jar" -b "$jar" "$api/csrf" | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -sS -i -c "$jar" -b "$jar" -H "X-CSRF-TOKEN: $token" --data-urlencode username=Alice --data-urlencode password=wrong "$api/login"
echo
curl -sS -i -c "$jar" -b "$jar" -H "X-CSRF-TOKEN: $token" --data-urlencode username=Alice --data-urlencode password=1111 "$api/login"
echo
curl -sS -i -b "$jar" "$api/session"
echo
curl -sS -i -b "$jar" "$api/employees/10001"
echo
token=$(curl -sS -c "$jar" -b "$jar" "$api/csrf" | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -sS -i -b "$jar" -X POST -H 'Content-Type: application/json' -H "X-CSRF-TOKEN: $token" \
  --data-binary @- "$api/employees" <<'JSON'
{"employeeName":"Walter","departmentId":3,"jobName":"ASSOCIATE","salary":230000,"entranceDate":"2018-04-01"}
JSON
echo
rm -f "$jar"
