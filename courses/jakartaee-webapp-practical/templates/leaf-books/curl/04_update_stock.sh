#!/usr/bin/env bash
# 在庫数を、読み取ったときの version を付けて更新する（楽観的ロック）
# StockService#updateStock は、送られた version が現在の version と違えば
# OptimisticLockException を送出し、OptimisticLockExceptionMapper が 409 に変換する
# 先に ▶ アプリ実行で書店 API と Customer Hub をデプロイしておく
# 期待する結果:
#   1つ目 ログイン（HTTP 200）
#   2つ目 HTTP/1.1 200 と在庫（bookId 1 の quantity と version）
#   3つ目 HTTP/1.1 200。読み取った version で更新し、コミットで version が 1 つ増える（応答は更新後の在庫）
#   4つ目 HTTP/1.1 409。同じ（古くなった）version でもう一度更新する
set -x
api=http://localhost:8080/leaf-books-api/api
jar=$(mktemp)
curl -sS -o /dev/null -w '%{http_code}\n' -c "$jar" -H 'Content-Type: application/json' \
  -X POST "$api/auth/login" -d '{"email":"alice@example.com","password":"password"}'
xsrf=$(grep XSRF-TOKEN "$jar" | awk '{print $7}')
stock=$(curl -sS -b "$jar" "$api/stocks/1")
echo "$stock"
version=$(echo "$stock" | sed -E 's/.*"version":([0-9]+).*/\1/')
echo
curl -sS -i -b "$jar" -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $xsrf" \
  -X PUT "$api/stocks/1" -d "{\"version\":$version,\"quantity\":10}"
echo
curl -sS -i -b "$jar" -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $xsrf" \
  -X PUT "$api/stocks/1" -d "{\"version\":$version,\"quantity\":20}"
echo
rm -f "$jar"
