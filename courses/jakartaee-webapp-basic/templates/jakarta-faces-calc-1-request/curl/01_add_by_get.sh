#!/usr/bin/env bash
# 何をするか: CalcAddByGetPage.xhtml を、URL のクエリ（param1=30&param2=10）付きの GET で開く
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: f:viewParam が 2 つの値を Double に変換し、f:viewAction の addByGet が動いて「計算結果 ---> 40.0」が出る
set -x
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-faces-calc-1-request/CalcAddByGetPage.xhtml?param1=30&param2=10" | grep -E '計算結果'
