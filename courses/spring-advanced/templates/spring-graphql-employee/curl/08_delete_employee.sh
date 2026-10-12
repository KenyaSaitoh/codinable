#!/usr/bin/env bash
# deleteEmployee（削除。削除したIDが返る）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"deleteEmployee":"10002"}}
# DB はインメモリ。アプリを起動し直すと初期データに戻る
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"mutation { deleteEmployee(id: 10002) }"}'
