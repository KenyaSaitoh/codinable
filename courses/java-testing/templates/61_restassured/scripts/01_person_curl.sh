#!/usr/bin/env bash
# 本物の spring-rest-person に curl で同じリクエストを送り、レスポンスを見る
# 前提: 先に アプリ実行で spring-rest-person を起動しておく（6.1.3 の演習）
# 6.2 のモックサーバーのレスポンス（62_wiremock の curl/03_persons.sh）と見比べるためのもの
set -x
curl -sS -i http://localhost:8082/persons/1
echo
curl -sS -i http://localhost:8082/persons/999
echo
curl -sS -i "http://localhost:8082/persons/query_by_age?lowerAge=30"
echo
curl -sS -i -X POST http://localhost:8082/persons -H "Content-Type: application/json" -d '{"personName":"Frank","age":36,"gender":"male"}'
echo
