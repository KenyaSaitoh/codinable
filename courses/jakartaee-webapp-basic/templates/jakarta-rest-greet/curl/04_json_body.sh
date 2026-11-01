#!/usr/bin/env bash
# 何をするか: JSON の本文を GreetingRequest に変換して受け取る（アノテーションの無い引数）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: HTTP/1.1 200 と「Good Evening Alice!」
set -x
curl -sS -i -X POST -H 'Content-Type: application/json' -d '{"name":"Alice"}' http://localhost:8080/jakarta-rest-greet/api/greet/evening
