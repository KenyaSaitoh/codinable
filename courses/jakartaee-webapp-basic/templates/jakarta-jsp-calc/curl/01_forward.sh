#!/usr/bin/env bash
# 何をするか: POST /add で計算し、フォワードで返る結果画面を見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200 と「計算結果 ---> 30.0」。URL は /add のまま（Location ヘッダーは無い）
set -x
curl -s -i -X POST http://localhost:8080/jakarta-jsp-calc/add -d param1=10 -d param2=20
