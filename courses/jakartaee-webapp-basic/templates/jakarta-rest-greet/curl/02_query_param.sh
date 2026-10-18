#!/usr/bin/env bash
# 何をするか: クエリ文字列から値を受け取る（@QueryParam）。/morning?name=Alice
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: HTTP/1.1 200 と「Good Morning Alice!」
set -x
curl -s -i "http://localhost:8080/jakarta-rest-greet/api/greet/morning?name=Alice"
