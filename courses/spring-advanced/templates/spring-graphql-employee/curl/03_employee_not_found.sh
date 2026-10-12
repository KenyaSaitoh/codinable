#!/usr/bin/env bash
# employee（存在しない場合。HTTPステータスは200のまま、errors配列で返る点がRESTと異なる）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"errors":[{"message":"社員が存在しません => 99999",...,"extensions":{"classification":"NOT_FOUND"}}],"data":{"employee":null}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ employee(id: 99999) { id employeeName } }"}'
