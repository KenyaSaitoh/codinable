#!/usr/bin/env bash
# 設定値が最終的にどう決まったかを確かめる（GET /api/settings）
# 前提: 先に ▶ アプリ実行で jakarta-config-employee をデプロイしておく
# 期待する結果:
#   HTTP/1.1 200 と、url・employeesUrl（${employee.api.url}/employees を展開した値）・
#   connectTimeoutMs 2000・readTimeoutMs 3000・hasToken false の JSON
#   readTimeoutSource には、値を読んだ設定ソース（microprofile-config.properties、序列値 100）が出る
set -x
curl -sS -i http://localhost:8080/jakarta-config-employee/api/settings
echo
