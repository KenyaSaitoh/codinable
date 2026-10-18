#!/usr/bin/env bash
# 何をするか: フォーム形式（application/x-www-form-urlencoded）の本文から値を受け取る（@FormParam）
#             続けて、同じ /afternoon に JSON の本文を送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 1 つ目は 200 と「Good Afternoon Alice!」
#              2 つ目は Content-Type が @Consumes と一致しないので 415 Unsupported Media Type
set -x
curl -s -i -X POST -d 'name=Alice' http://localhost:8080/jakarta-rest-greet/api/greet/afternoon
echo
curl -s -o /dev/null -w '%{http_code}\n' -X POST -H 'Content-Type: application/json' -d '{"name":"Alice"}' http://localhost:8080/jakarta-rest-greet/api/greet/afternoon
