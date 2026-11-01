#!/usr/bin/env bash
# 削除（DELETE /api/users/1）→ 削除したユーザーを取得し直す
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# データはメモリ内。アプリを起動し直すと初期状態に戻る
# 期待する結果:
#   1つ目 HTTP/1.1 204（返す本文が無い）
#   2つ目 HTTP/1.1 404
set -x
curl -sS -i -X DELETE http://localhost:8080/api/users/1
echo
curl -sS -i http://localhost:8080/api/users/1
