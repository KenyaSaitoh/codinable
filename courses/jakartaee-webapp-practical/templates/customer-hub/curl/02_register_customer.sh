#!/usr/bin/env bash
# 顧客を登録してから、同じメールアドレスでもう一度登録する
# CustomerService が CustomerExistsException を送出し、CustomerExceptionMapper が 409 に変換する
# 書店の新規登録（AuthResource#register）は、この 409 をメールアドレスの重複として扱う
# 先に ▶ アプリ実行で Customer Hub をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と登録した顧客（customerId が振られる）
#   2つ目 HTTP/1.1 409 と customer.exists
set -x
api=http://localhost:8080/customer-hub-api/customers
# JSON は標準入力から渡す（日本語をコマンドの引数に書くと、Windows では文字コードが変わってしまう）
register() {
  curl -sS -i -H 'Content-Type: application/json' -X POST "$api/" --data-binary @- <<'JSON'
{"customerName":"Frank","password":"password","email":"frank@example.com","birthday":"2000-01-01","address":"東京都港区1-2-3"}
JSON
  echo
}
register
register
