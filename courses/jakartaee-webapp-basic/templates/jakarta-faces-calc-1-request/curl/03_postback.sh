#!/usr/bin/env bash
# 何をするか: 入力画面を GET で開き、その画面自身へフォームを POST で送る（ポストバック）
#             Faces のフォームは、入力欄の名前が calcForm:param1 のようなクライアントIDになり、
#             画面の状態を表す jakarta.faces.ViewState も一緒に送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 「計算結果 ---> 40.0」を含む CalcOutputPage の HTML（URL は CalcInputPage.xhtml のまま）
URL=http://localhost:8080/jakarta-faces-calc-1-request/CalcInputPage.xhtml
jar=$(mktemp)
set -x
page=$(curl -s -c "$jar" "$URL")
viewState=$(echo "$page" | sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p')
echo "$page" | grep -o 'name="calcForm:[a-zA-Z0-9]*"'
curl -s -b "$jar" "$URL" \
  --data-urlencode "calcForm=calcForm" \
  --data-urlencode "calcForm:param1=30" \
  --data-urlencode "calcForm:param2=10" \
  --data-urlencode "calcForm:addButton=足し算" \
  --data-urlencode "jakarta.faces.ViewState=$viewState" | grep -E '<h2>|計算結果'
