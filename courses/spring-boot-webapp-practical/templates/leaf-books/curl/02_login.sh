#!/usr/bin/env bash
# ログインして、書籍一覧と注文履歴を取得する
# JWT は HttpOnly Cookie（token）で、更新系の要求には CSRF トークン
# （Cookie XSRF-TOKEN の値をヘッダー X-XSRF-TOKEN に）が要る
# Cookie は一時ファイル（cookie jar）に保存して、次の要求へ持ち回る
# 先に ▶ アプリ実行で REST API（8080）と Customer Hub（customer-hub/、8081）を起動しておく
# 期待する結果:
#   1つ目 HTTP/1.1 403（本体は 403 だが XSRF-TOKEN の Cookie を受け取る）
#   2つ目 HTTP/1.1 200 と Set-Cookie: token=...（JWT）。alice@gmail.com / password でログイン
#   3つ目 HTTP/1.1 200 と書籍の一覧
#   4つ目 HTTP/1.1 200 と alice の注文履歴
set -x
jar=$(mktemp)
curl -sS -s -i -c "$jar" -b "$jar" -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/books
xsrf=$(grep XSRF-TOKEN "$jar" | awk '{print $7}')
echo
curl -sS -i -c "$jar" -b "$jar" -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $xsrf" \
  -X POST http://localhost:8080/api/auth/login \
  -d '{"email":"alice@gmail.com","password":"password"}'
echo
curl -sS -i -b "$jar" http://localhost:8080/api/books
echo
curl -sS -i -b "$jar" http://localhost:8080/api/orders/history
rm -f "$jar"
