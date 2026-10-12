#!/usr/bin/env bash
# 非公開の検索メソッドは404、マスターへの書き込みは405
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果:
#   1つ目 HTTP/1.1 404
#   2つ目 HTTP/1.1 405
set -x
curl -sS -i 'http://localhost:8080/api/employees/search/findByStatus?status=active'
echo
curl -sS -i -X DELETE http://localhost:8080/api/departments/1
