#!/usr/bin/env bash
# 何をするか: param2 を送らずに POST /add を呼ぶ
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: getParameter("param2") が null になり、sendError で 400 Bad Request が返る
set -x
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -X POST http://localhost:8080/jakarta-servlet-calc/add -d param1=10
