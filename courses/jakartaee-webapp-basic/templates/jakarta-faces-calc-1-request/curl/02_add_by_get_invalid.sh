#!/usr/bin/env bash
# 何をするか: CalcAddByGetPage.xhtml を、変換できない値（param1=abc）と、param2 の無いクエリで開く
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: どちらも h:messages に赤いエラーが出て、計算結果は空のまま（f:viewAction は呼ばれない）
set -x
curl -s "http://localhost:8080/jakarta-faces-calc-1-request/CalcAddByGetPage.xhtml?param1=abc&param2=10" | grep -E '<li|計算結果'
curl -s "http://localhost:8080/jakarta-faces-calc-1-request/CalcAddByGetPage.xhtml?param1=30" | grep -E '<li|計算結果'
