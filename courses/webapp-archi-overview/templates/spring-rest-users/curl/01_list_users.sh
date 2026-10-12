#!/usr/bin/env bash
# 一覧取得（GET /api/users）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# データはメモリ内。アプリを起動し直すと初期状態に戻る
# 期待する結果: HTTP/1.1 200 と、ユーザーの JSON 配列
set -x
curl -sS -i http://localhost:8080/api/users
