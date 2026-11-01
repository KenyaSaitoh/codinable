#!/usr/bin/env bash
# 何をするか: 入力画面へ、いくつかの組み合わせでポストバックを送り、どの段階で止まるかを見る
#   空欄 / abc / 1001 / -1000 と 1000（境界値） / 30 と 10
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   空欄       → 「値を入力してください」（@NotNull）
#   abc        → 「パラメータ1は数値で入力してください」（BigDecimal への変換。CalcMessages.properties の文言）
#   1001       → 「入力可能な最大値は1000」（@Max）
#   -1000/1000 → 範囲内なので計算される（計算結果 => 0）
#   30/10      → 計算結果 => 40
URL=http://localhost:8080/jakarta-faces-calc-2-validation/CalcInputPage.xhtml
post() {
  local jar viewState
  jar=$(mktemp)
  viewState=$(curl -sS -c "$jar" "$URL" | sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p')
  echo "--- param1=[$1] param2=[$2]"
  curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -b "$jar" "$URL" \
    --data-urlencode "calcForm=calcForm" \
    --data-urlencode "calcForm:param1=$1" \
    --data-urlencode "calcForm:param2=$2" \
    --data-urlencode "calcForm:addButton=足し算" \
    --data-urlencode "jakarta.faces.ViewState=$viewState" \
    | grep -oE '<span style="color: red">[^<]*</span>|計算結果 =&gt; [^<]*'
}
set -x
post "" 10
post abc 10
post 1001 10
post -1000 1000
post 30 10
