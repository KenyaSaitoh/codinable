#!/usr/bin/env bash
# 何をするか: 4 つのボタンで、業務検証と業務例外が起きる入力を順に送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   足し算 0 と 10      → 「パラメータ1の値がゼロです」（項目のエラー。rejectZero）
#   引き算 5 と 5       → 「同一の値同士の引き算は認められていません」（グローバルなエラー）
#   掛け算 1000 と 101  → CalcErrorPage「1,000と101の掛け算で、極度オーバーが発生しました」（LimitOverException）
#   割り算 10 と 0      → CalcErrorPage「ゼロ割が発生しました」（ZeroDivideException）
#   割り算 10 と 3      → 計算結果 => 3.3333（calc.result.scale=4 桁に丸める）
#   実行結果には LoggingInterceptor の「[ CalcService#divide ] start」「… end 0ms」のログが出る
URL=http://localhost:8080/jakarta-faces-calc-3-config-exception/CalcInputPage.xhtml
post() {
  local jar viewState
  jar=$(mktemp)
  viewState=$(curl -sS -c "$jar" "$URL" | sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p')
  echo "--- $1: param1=[$2] param2=[$3]"
  curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -b "$jar" "$URL" \
    --data-urlencode "calcForm=calcForm" \
    --data-urlencode "calcForm:param1=$2" \
    --data-urlencode "calcForm:param2=$3" \
    --data-urlencode "calcForm:$1=x" \
    --data-urlencode "jakarta.faces.ViewState=$viewState" \
    | grep -oE '<span style="color: red">[^<]*</span>|<li[^>]*>[^<]*</li>|詳細情報：[^<]*|計算結果 =&gt; [^<]*'
}
set -x
post addButton 0 10
post subtractButton 5 5
post multiplyButton 1000 101
post divideButton 10 0
post divideButton 10 3
