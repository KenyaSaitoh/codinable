#!/usr/bin/env bash
# 1 件取得（GET /api/users/1）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# データはメモリ内。アプリを起動し直すと初期状態に戻る
# 期待する結果: HTTP/1.1 200 と、ID 1 のユーザーの JSON（見つからない ID なら 404）
set -x
curl -sS -i http://localhost:8080/api/users/1
