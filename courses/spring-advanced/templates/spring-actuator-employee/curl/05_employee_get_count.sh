#!/usr/bin/env bash
# metrics（カスタムメトリクス：社員取得の呼び出し回数）
# 01_call_api.sh を実行する前後で、COUNT が増えることを確かめる
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"name":"employee.get.count","measurements":[{"statistic":"COUNT","value":1.0}],...}
set -x
curl -sS -i localhost:8080/actuator/metrics/employee.get.count
