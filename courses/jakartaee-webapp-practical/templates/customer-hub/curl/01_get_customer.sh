#!/usr/bin/env bash
# 顧客を ID とメールアドレスで照会する
# 書店（leaf-books）の CustomerHubRestClient は、ログインのときにメールアドレスでの照会を呼ぶ
# 先に ▶ アプリ実行で Customer Hub（/customer-hub-api）をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と顧客 1（Alice）の JSON
#   2つ目 HTTP/1.1 200。alice@example.com で照会した結果
#   3つ目 HTTP/1.1 404 と customer.not-found（登録されていないメールアドレス）
set -x
api=http://localhost:8080/customer-hub-api/customers
curl -sS -i "$api/1"
echo
curl -sS -i "$api/query_email?email=alice@example.com"
echo
curl -sS -i "$api/query_email?email=nobody@example.com"
echo
