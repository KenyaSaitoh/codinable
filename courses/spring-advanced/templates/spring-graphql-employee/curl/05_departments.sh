#!/usr/bin/env bash
# departments（全部署取得。部署内の社員は @BatchMapping でまとめて取得される）
# 出力のSQLログで、部署ごとにSQLが発行されない（N+1問題が起きない）ことを確かめる
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"departments":[{"id":"1","departmentName":"PLANNING","location":"TOKYO HQ","employees":[{"employeeName":"Bob"},...]},...]}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ departments { id departmentName location employees { employeeName } } }"}'
