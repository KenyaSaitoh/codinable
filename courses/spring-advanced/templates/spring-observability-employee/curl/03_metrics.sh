#!/usr/bin/env bash
# metrics（Observationから生成されたメトリクスの確認：メトリクス名一覧）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"names":["employee.get","employee.get-all","http.server.requests","jvm.memory.used",...]}
#   （employee.get と employee.get-all は 01・02 を実行したあとに現れる）
set -x
curl -sS -i localhost:8080/actuator/metrics
