#!/usr/bin/env bash
# 何をするか: 一覧の「新規登録」→ 入力画面（空欄で確認 → エラー）→ 入力して確認 → 確定、を同じ Cookie で順に進む
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   1. 「新規登録」の POST は 302 で、入力画面の URL に cid（会話ID）が付く（toCreate が会話を開始）
#   2. 氏名を空欄で「確認」すると、入力画面に必須のエラーが出る（@NotEmpty）
#   3. 入力して「確認」すると、確認画面に入力値が出る（同じ会話の EmployeeEditBean）
#   4. 「確定」で一覧へリダイレクトし、10017 の Zoe が増えている（update が会話を終了）
BASE=http://localhost:8080/jakarta-faces-employee
jar=$(mktemp)
vs() { sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p'; }
act() { sed -n "s/.*<form id=\"$1\"[^>]*action=\"\([^\"]*\)\".*/\1/p" | sed 's/&amp;/\&/g'; }
loc() { sed -n 's/^Location: *//Ip' | tr -d '\r'; }
set -x
page=$(curl -sS -c "$jar" -b "$jar" "$BASE/EmployeeTablePage.xhtml")
input=$(curl -sS -D - -o /dev/null -c "$jar" -b "$jar" "http://localhost:8080$(echo "$page" | act tableForm)" \
  --data-urlencode "tableForm=tableForm" --data-urlencode "tableForm:createButton=x" \
  --data-urlencode "jakarta.faces.ViewState=$(echo "$page" | vs)" | loc)
echo "入力画面: $input"
page=$(curl -sS -c "$jar" -b "$jar" "$input")
send() {
  curl -sS -c "$jar" -b "$jar" "http://localhost:8080$(echo "$page" | act inputForm)" -D - \
    --data-urlencode "inputForm=inputForm" \
    --data-urlencode "inputForm:employeeName=$1" \
    --data-urlencode "inputForm:departmentName=$2" \
    --data-urlencode "inputForm:salary=$3" \
    --data-urlencode "inputForm:confirmButton=x" \
    --data-urlencode "jakarta.faces.ViewState=$(echo "$page" | vs)"
}
send "" SALES 300000 | grep -oE '<span style="color: red">[^<]*</span>'
confirm=$(send Zoe SALES 300000 | loc)
echo "確認画面: $confirm"
page=$(curl -sS -c "$jar" -b "$jar" "$confirm")
echo "$page" | grep -E '<div>'
table=$(curl -sS -D - -o /dev/null -c "$jar" -b "$jar" "http://localhost:8080$(echo "$page" | act confirmForm)" \
  --data-urlencode "confirmForm=confirmForm" --data-urlencode "confirmForm:updateButton=x" \
  --data-urlencode "jakarta.faces.ViewState=$(echo "$page" | vs)" | loc)
echo "一覧: $table"
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "$table" | grep -A3 '10017'
