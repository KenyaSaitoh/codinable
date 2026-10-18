#!/usr/bin/env bash
# 何をするか: リクエストヘッダーから値を受け取る（@HeaderParam）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: HTTP/1.1 200 と「Good Night Alice!」
set -x
curl -sS -i -H 'name: Alice' http://localhost:8080/jakarta-rest-greet/api/greet/night
