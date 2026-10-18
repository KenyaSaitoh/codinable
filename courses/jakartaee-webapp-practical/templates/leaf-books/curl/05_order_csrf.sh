#!/usr/bin/env bash
# 注文を、CSRF トークンを付けずに送ってから、付けて送り直す
# CsrfTokenFilter は更新系の要求で、Cookie XSRF-TOKEN とヘッダー X-XSRF-TOKEN の値が一致しなければ 403 を返す
# JWT の Cookie は Webブラウザが自動で送るので、Cookie だけでは「本人の画面からの要求か」を区別できない
# 先に ▶ アプリ実行で書店 API と Customer Hub をデプロイしておく
# 期待する結果:
#   1つ目 ログイン（HTTP 200）
#   2つ目 HTTP/1.1 403（X-XSRF-TOKEN ヘッダーが無い）
#   3つ目 HTTP/1.1 200 と注文の結果（送料 800 円。在庫が 1 冊減る）
set -x
api=http://localhost:8080/leaf-books-api/api
jar=$(mktemp)
curl -sS -o /dev/null -w '%{http_code}\n' -c "$jar" -H 'Content-Type: application/json' \
  -X POST "$api/auth/login" -d '{"email":"alice@example.com","password":"password"}'
xsrf=$(grep XSRF-TOKEN "$jar" | awk '{print $7}')
# 注文の JSON は標準入力から渡す（日本語をコマンドの引数に書くと、Windows では文字コードが変わってしまう）
order() {
  curl -sS -i -b "$jar" -H 'Content-Type: application/json' "$@" -X POST "$api/orders" --data-binary @- <<'JSON'
{"cartItems":[{"bookId":2,"bookName":"JVMとバイトコードの探求","publisherName":"デジタルフロンティア出版","price":4200,"count":1,"version":0}],
 "totalPrice":4200,"deliveryPrice":800,"deliveryAddress":"東京都中央区1-1-1","settlementType":1}
JSON
  echo
}
order
order -H "X-XSRF-TOKEN: $xsrf"
rm -f "$jar"
