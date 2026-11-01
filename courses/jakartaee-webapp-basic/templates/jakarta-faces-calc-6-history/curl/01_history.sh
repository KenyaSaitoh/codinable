#!/usr/bin/env bash
# 何をするか: 足し算と、上限を超える掛け算を送り、リダイレクト先の URL と履歴を確かめる
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと履歴は空に戻る）
# 期待する結果:
#   1. 足し算の POST は 302 で、Location は …/CalcOutputPage.xhtml?id=1（結果の値ではなく履歴の ID）
#   2. その URL を何度開いても同じ結果が出る（保存済みデータを ID で取り直している）
#   3. 掛け算 1000 × 101 は CalcErrorPage になるが、履歴には ID 2 として結果が残る（保存してから上限を判定するため）
#   4. 存在しない id=99 は CalcNotFoundPage へ移る、id=abc は変換エラー、id なしは「値が必要です」のエラー
BASE=http://localhost:8080/jakarta-faces-calc-6-history
vs() { sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p'; }
post() {
  local jar viewState
  jar=$(mktemp)
  viewState=$(curl -sS -c "$jar" "$BASE/CalcInputPage.xhtml" | vs)
  curl -sS -b "$jar" "$BASE/CalcInputPage.xhtml" -D - \
    --data-urlencode "calcForm=calcForm" \
    --data-urlencode "calcForm:param1=$2" \
    --data-urlencode "calcForm:param2=$3" \
    --data-urlencode "calcForm:$1=x" \
    --data-urlencode "jakarta.faces.ViewState=$viewState" | grep -E '^Location|詳細情報'
}
set -x
post addButton 30 10
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "$BASE/CalcOutputPage.xhtml?id=1" | grep -E '足し算|計算日時'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "$BASE/CalcOutputPage.xhtml?id=1" | grep -E '足し算|計算日時'
post multiplyButton 1000 101
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "$BASE/CalcHistoryPage.xhtml" | tr -d '\n' | sed 's#</tr>#\n#g' | grep '<td>' | sed 's/<[^>]*>/ /g; s/  */ /g'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -L "$BASE/CalcOutputPage.xhtml?id=99" | grep -E '<h2>|見つかりません'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "$BASE/CalcOutputPage.xhtml?id=abc" | grep -E '<li'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "$BASE/CalcOutputPage.xhtml" | grep -E '<li'
