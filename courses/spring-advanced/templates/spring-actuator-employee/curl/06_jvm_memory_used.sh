#!/usr/bin/env bash
# metrics（JVMのヒープ使用量）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"name":"jvm.memory.used","baseUnit":"bytes","measurements":[{"statistic":"VALUE","value":1.234567E8}],...}
set -x
curl -sS -i localhost:8080/actuator/metrics/jvm.memory.used
