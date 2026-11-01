#!/usr/bin/env bash
# 論理削除。成功は204で本文なし。再取得は404
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果:
#   1つ目 HTTP/1.1 204
#   2つ目 HTTP/1.1 404
set -x
curl -sS -i -X DELETE http://127.0.0.1:8080/employees/10017
echo
curl -sS -i http://127.0.0.1:8080/employees/10017
