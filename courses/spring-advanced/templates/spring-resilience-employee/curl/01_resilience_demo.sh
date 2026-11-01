#!/usr/bin/env bash
# resilienceDemo（正常系。呼び出し先 API の稼働中は普通に社員リストが返る）→ 回路の状態を見る
# 先に ▶ アプリ実行でアプリ（8082）と呼び出し先 API（api/、8095）を起動しておく
# 期待する結果:
#   1つ目 HTTP/1.1 200
#     {"employees":[...16件...],"fallback":false,"message":"取得成功"}
#   2つ目 HTTP/1.1 200 と回路の状態（最初は CLOSED）
set -x
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-status
