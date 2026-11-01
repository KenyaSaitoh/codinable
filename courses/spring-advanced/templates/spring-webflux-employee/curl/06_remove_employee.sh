#!/usr/bin/env bash
# removeEmployee（削除は204。削除後に取得すると404）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果:
#   1つ目 HTTP/1.1 204
#   2つ目 HTTP/1.1 404
set -x
curl -sS -i -X DELETE localhost:8089/employees/10002
echo
curl -sS -i localhost:8089/employees/10002
