#!/usr/bin/env bash
# 何をするか: WEB-INF の下の JSP を、サーブレットを経ずに直接要求する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 404。WEB-INF の中は Webブラウザから直接参照できない
set -x
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/jakarta-jsp-calc/WEB-INF/views/calcOutput.jsp
