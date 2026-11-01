#!/usr/bin/env bash
# Projection。通常の社員JSONと比較する
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果: どちらも HTTP/1.1 200
#   2つ目（?projection=summary）は月給・入社日が省かれている
set -x
curl -sS -i http://localhost:8080/api/employees/10001
echo
curl -sS -i 'http://localhost:8080/api/employees/10001?projection=summary'
