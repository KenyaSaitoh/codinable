#!/usr/bin/env bash
# metrics（HTTPリクエストの統計。URIやステータスごとのタグで絞り込みも可能）
# タグの値 uri:/employees/{employeeId} は --data-urlencode で URL エンコードして送る
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"name":"http.server.requests","measurements":[{"statistic":"COUNT","value":2.0},{"statistic":"TOTAL_TIME","value":0.123},...]}
set -x
curl -sS -i --get localhost:8080/actuator/metrics/http.server.requests --data-urlencode 'tag=uri:/employees/{employeeId}'
