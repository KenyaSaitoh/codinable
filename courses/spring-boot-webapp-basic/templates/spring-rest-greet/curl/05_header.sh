#!/usr/bin/env bash
# リクエストヘッダーから値を受け取る（@RequestHeader）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200 と、名前 Alice を含むあいさつ
set -x
curl -sS -i -H 'name: Alice' localhost:8080/greet/night
