#!/usr/bin/env bash
# 何をするか: パスから値を受け取る（@PathParam）。/hello/Alice と /goodbye/Alice
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: どちらも HTTP/1.1 200 と、名前 Alice を含むあいさつ
set -x
curl -sS -i http://localhost:8080/jakarta-rest-greet/api/greet/hello/Alice
echo
curl -sS -i http://localhost:8080/jakarta-rest-greet/api/greet/goodbye/Alice
