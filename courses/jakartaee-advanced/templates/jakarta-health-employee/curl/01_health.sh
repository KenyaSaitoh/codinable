#!/usr/bin/env bash
# ヘルスのエンドポイントを問い合わせる（Payara の場合、サーバーのルートコンテキストの /health に置かれる）
# 前提: 先に ▶ アプリ実行で jakarta-health-employee をデプロイしておく
# 期待する結果:
#   1つ目 GET /health は HTTP/1.1 200 と {"status":"UP","checks":[...]}。
#         checks に heap-memory（usedBytes・maxBytes）、database（employeeCount 16）、initialization が入る
#   2つ目 GET /health/live は heap-memory だけ（@Liveness）
#   3つ目 GET /health/ready は database だけ（@Readiness）
#   4つ目 GET /health/started は initialization だけ（@Startup）
set -x
curl -sS -i http://localhost:8080/health
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' http://localhost:8080/health/live
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' http://localhost:8080/health/ready
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' http://localhost:8080/health/started
echo
