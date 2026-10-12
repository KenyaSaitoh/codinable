#!/usr/bin/env bash
# metrics（収集されているメトリクス名の一覧）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"names":["employee.get.count","http.server.requests","jvm.memory.used","process.cpu.usage",...]}
set -x
curl -sS -i localhost:8080/actuator/metrics
