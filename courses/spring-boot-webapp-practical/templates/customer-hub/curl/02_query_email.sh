#!/usr/bin/env bash
# メールアドレスで照会する（ログイン時に書店が使う）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8081）
# DB はインメモリ。アプリを起動し直すと初期データに戻る
# 期待する結果: HTTP/1.1 200 と alice の JSON
set -x
curl -sS -i 'http://localhost:8081/customers/query_email?email=alice@gmail.com'
