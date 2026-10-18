#!/usr/bin/env bash
# 何をするか: 同じ Cookie（JSESSIONID）を持ち回って、計算 → 入力画面の表示 を続けて行う
#             最後に Cookie を持たない要求で入力画面を開き、別のセッションになることを見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 2 回目の入力画面には「前回の計算結果 ---> 30.0」が出る。Cookie なしの 3 回目には出ない
jar=$(mktemp)
set -x
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -c "$jar" -X POST http://localhost:8080/jakarta-jsp-calc/add -d param1=10 -d param2=20 > /dev/null
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -b "$jar" http://localhost:8080/jakarta-jsp-calc/calc | grep -E '前回の計算結果' || echo '（前回の計算結果は出ていない）'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' http://localhost:8080/jakarta-jsp-calc/calc | grep -E '前回の計算結果' || echo '（前回の計算結果は出ていない）'
