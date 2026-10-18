#!/usr/bin/env bash
# 何をするか: 社員 10001 を取得して ETag を見る。続けて If-None-Match に同じ ETag を付けて取得する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 1 つ目は 200 と ETag: "0"（version の値）。2 つ目は 304 Not Modified（本文なし）
#              存在しない 99999 は 404 と {"status":404,"message":"該当する社員が見つかりません",…}
set -x
curl -sS -i http://localhost:8080/jakarta-rest-employee/api/employees/10001
echo
curl -sS -i -H 'If-None-Match: "0"' http://localhost:8080/jakarta-rest-employee/api/employees/10001
echo
curl -sS -i http://localhost:8080/jakarta-rest-employee/api/employees/99999
