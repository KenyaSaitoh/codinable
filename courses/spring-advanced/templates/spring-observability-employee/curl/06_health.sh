#!/usr/bin/env bash
# health（稼働確認）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"status":"UP"}
# トレースの確認（Jaegerを立てている場合。README.md の手順で Docker から起動する）
#   リクエストを何回か送り、ブラウザで http://localhost:16686 を開いてサービス名
#   spring-observability-employee を選択すると、
#   HTTP受信 → employee.get → SQL発行 がスパンの階層として表示される
set -x
curl -sS -i localhost:8080/actuator/health
