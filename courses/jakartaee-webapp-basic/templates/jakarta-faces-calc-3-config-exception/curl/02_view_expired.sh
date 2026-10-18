#!/usr/bin/env bash
# 何をするか: セッションの Cookie を持たずに、存在しない画面の状態（jakarta.faces.ViewState）でポストバックする
#             サーバーに画面の状態が無いので、Faces は ViewExpiredException を送出する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: CalcExceptionHandler が ViewExpiredPage へのリダイレクトに変換する
#              （302 と Location: …/ViewExpiredPage.xhtml。-L で移動先の本文も出す）
set -x
curl -sS -i -L http://localhost:8080/jakarta-faces-calc-3-config-exception/CalcInputPage.xhtml \
  --data-urlencode "calcForm=calcForm" \
  --data-urlencode "calcForm:param1=30" \
  --data-urlencode "calcForm:param2=10" \
  --data-urlencode "calcForm:addButton=x" \
  --data-urlencode "jakarta.faces.ViewState=-1234567890:9876543210" \
  | grep -E '^HTTP|^Location|<h2>|<div>'
