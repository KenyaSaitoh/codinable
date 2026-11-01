#!/usr/bin/env bash
# 何をするか: 入力画面からポストバックで足し算を送り、応答と、リダイレクト先の GET を 2 回続けて見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   1. POST の応答は 302 と Location: …/CalcOutputPage.xhtml（faces-redirect=true。結果の値は URL に付かない）
#   2. 同じ Cookie で Location を GET すると「計算結果：40」（Flash に入れた値）
#   3. もう一度 GET する（再読み込みと同じ）と、計算結果は空になる（Flash の値は 1 回の描画で破棄される）
URL=http://localhost:8080/jakarta-faces-calc-4-prg/CalcInputPage.xhtml
jar=$(mktemp)
set -x
viewState=$(curl -sS -c "$jar" -b "$jar" "$URL" | sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p')
headers=$(curl -sS -D - -o /dev/null -c "$jar" -b "$jar" "$URL" \
  --data-urlencode "calcForm=calcForm" \
  --data-urlencode "calcForm:param1=30" \
  --data-urlencode "calcForm:param2=10" \
  --data-urlencode "calcForm:addButton=x" \
  --data-urlencode "jakarta.faces.ViewState=$viewState")
echo "$headers" | grep -E "^HTTP|^Location"
location=$(echo "$headers" | sed -n 's/^Location: *//Ip' | tr -d '\r')
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "$location" | grep '計算結果'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "$location" | grep '計算結果'
