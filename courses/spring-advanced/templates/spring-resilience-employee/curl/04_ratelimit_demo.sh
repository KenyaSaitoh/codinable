#!/usr/bin/env bash
# rateLimitDemo（レートリミッター。10秒あたり3回まで）を短時間に4回呼ぶ
# 先に ▶ アプリ実行でアプリ（8082）と呼び出し先 API（api/、8095）を起動しておく
# 期待する結果:
#   1〜3回目 HTTP/1.1 200 と {"employeeId":10001,"employeeName":"Alice",...}
#   4回目    HTTP/1.1 429 と「レートリミッターにより呼び出しが制限されました」
#   （10秒待つと許可回数がリフレッシュされ、再び呼び出せる）
set -x
curl -sS -i localhost:8082/ratelimit-demo
echo
curl -sS -i localhost:8082/ratelimit-demo
echo
curl -sS -i localhost:8082/ratelimit-demo
echo
curl -sS -i localhost:8082/ratelimit-demo
