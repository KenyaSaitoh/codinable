#!/usr/bin/env bash
# ログインして、注文履歴を取得する
# AuthResource#login は Customer Hub に顧客を照会し、パスワードを照合してから
# JWT の Cookie（leaf-books-jwt、HttpOnly）と CSRF トークンの Cookie（XSRF-TOKEN）を返す
# Cookie は一時ファイル（cookie jar）に保存して、次の要求へ持ち回る
# 先に ▶ アプリ実行で書店 API と Customer Hub をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と Set-Cookie が 2 つ（leaf-books-jwt と XSRF-TOKEN）。alice@example.com / password でログイン
#   2つ目 HTTP/1.1 200 と alice の注文履歴（初期データの注文）
set -x
api=http://localhost:8080/leaf-books-api/api
jar=$(mktemp)
curl -sS -i -c "$jar" -H 'Content-Type: application/json' \
  -X POST "$api/auth/login" \
  -d '{"email":"alice@example.com","password":"password"}'
echo
curl -sS -i -b "$jar" "$api/orders/history"
echo
rm -f "$jar"
