#!/usr/bin/env bash
# 登録（POST /api/users）。作ったものの場所が Location ヘッダーで返る
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# データはメモリ内。アプリを起動し直すと初期状態に戻る
# 期待する結果: HTTP/1.1 201
#   Location: /api/users/<採番された ID>
#   本文は登録された Carol の JSON
set -x
curl -sS -i -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Carol","email":"carol@example.com"}'
