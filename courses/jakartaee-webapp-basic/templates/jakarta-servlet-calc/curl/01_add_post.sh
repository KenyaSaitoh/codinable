#!/usr/bin/env bash
# 何をするか: 入力画面の「足し算(POST)」と同じ要求を送る（param1 と param2 を本文に入れて POST /add）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200 と、「計算結果 ---> 30.0」を含む HTML
set -x
curl -s -i -X POST http://localhost:8080/jakarta-servlet-calc/add -d param1=10 -d param2=20
