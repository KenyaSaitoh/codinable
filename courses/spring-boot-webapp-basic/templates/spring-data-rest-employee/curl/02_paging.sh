#!/usr/bin/env bash
# HALの_embedded・_links・pageを見る（pageは0始まり）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果: どちらも HTTP/1.1 200。_embedded.employees に 5 件、page に size・totalElements・totalPages・number
#   _links の next / prev で前後のページへたどれる
set -x
curl -sS -i 'http://localhost:8080/api/employees?page=0&size=5&sort=employeeId,asc'
echo
curl -sS -i 'http://localhost:8080/api/employees?page=1&size=5&sort=employeeId,asc'
