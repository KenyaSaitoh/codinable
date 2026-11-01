#!/usr/bin/env bash
# metrics（フレームワークが自動計装したHTTPリクエストの統計）
# タグの値 uri:/employees/{employeeId} は --data-urlencode で URL エンコードして送る
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"name":"http.server.requests","measurements":[{"statistic":"COUNT","value":1.0},...]}
set -x
curl -sS -i --get localhost:8080/actuator/metrics/http.server.requests --data-urlencode 'tag=uri:/employees/{employeeId}'
