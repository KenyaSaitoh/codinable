#!/usr/bin/env bash
# JSON 本文から値を受け取る（@RequestBody で GreetingRequest に変換）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200 と、名前 Alice を含むあいさつ
set -x
curl -sS -i -X POST -H 'Content-Type: application/json' -d '{"name":"Alice"}' localhost:8080/greet/evening
