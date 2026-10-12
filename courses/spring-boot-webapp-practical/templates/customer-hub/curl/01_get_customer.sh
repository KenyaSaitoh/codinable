#!/usr/bin/env bash
# 顧客 ID で取得する。存在しない ID は 404
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8081）
# DB はインメモリ。アプリを起動し直すと初期データに戻る
# 期待する結果:
#   1つ目 HTTP/1.1 200 と顧客 1 の JSON
#   2つ目 HTTP/1.1 404
set -x
curl -sS -i http://localhost:8081/customers/1
echo
curl -sS -i http://localhost:8081/customers/999
