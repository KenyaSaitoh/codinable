#!/usr/bin/env bash
# 部署の一覧
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
set -x
curl -sS -i http://localhost:8080/departments
