#!/usr/bin/env bash
# 何をするか: 「足し算(GET)」と同じ要求を送る（param1 と param2 を URL のクエリ文字列に入れて GET /addByGet）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200 と、「計算結果 ---> 30.0」を含む HTML（同じ calculate が呼ばれる）
set -x
curl -s -i "http://localhost:8080/jakarta-servlet-calc/addByGet?param1=10&param2=20"
