#!/usr/bin/env bash
# 何をするか: 足し算 → 結果画面 → 税込計算 → 税込金額の画面、を同じ Cookie で順に進む
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   1. 足し算の POST は 302 で、Location の URL に cid（会話ID）が付く
#   2. その URL の結果画面に「計算結果 => 40.0000」と会話ID が出る（同じ会話の CalcConversation）
#   3. 税込計算の POST も 302。Location は TaxOutputPage.xhtml（会話は終わったので cid は付かない）
#   4. 税込金額の画面に「税込金額 => 44」（40 × 1.1 の 1 円未満を切り捨て。値は Flash で届く）
#   5. 終了した会話の cid を付けて結果画面を開くと、NonexistentConversationException の 500 になる
BASE=http://localhost:8080/jakarta-faces-calc-5-conversation
jar=$(mktemp)
vs() { sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p'; }
loc() { sed -n 's/^Location: *//Ip' | tr -d '\r'; }
set -x
viewState=$(curl -sS -c "$jar" -b "$jar" "$BASE/CalcInputPage.xhtml" | vs)
out=$(curl -sS -c "$jar" -b "$jar" "$BASE/CalcInputPage.xhtml" -D - -o /dev/null \
  --data-urlencode "calcForm=calcForm" \
  --data-urlencode "calcForm:param1=30" \
  --data-urlencode "calcForm:param2=10" \
  --data-urlencode "calcForm:addButton=x" \
  --data-urlencode "jakarta.faces.ViewState=$viewState" | loc)
echo "Location: $out"
page=$(curl -sS -c "$jar" -b "$jar" "$out")
echo "$page" | grep -E '計算結果|会話ID'
action=$(echo "$page" | sed -n 's/.*<form id="taxForm"[^>]*action="\([^"]*\)".*/\1/p' | sed 's/&amp;/\&/g')
out2=$(curl -sS -c "$jar" -b "$jar" "http://localhost:8080$action" -D - -o /dev/null \
  --data-urlencode "taxForm=taxForm" \
  --data-urlencode "taxForm:calcTaxButton=x" \
  --data-urlencode "jakarta.faces.ViewState=$(echo "$page" | vs)" | loc)
echo "Location: $out2"
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "$out2" | grep -E '税込金額'
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "$out"
