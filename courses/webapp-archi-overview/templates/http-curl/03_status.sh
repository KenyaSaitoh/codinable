#!/usr/bin/env bash
# ステータスコードの意味を、実際にその番号を返させて確かめる
set -u

for code in 200 201 204 301 400 401 403 404 500 503; do
  # -o /dev/null で本文を捨て、-w で書式を指定して知りたい値だけ出す
  printf '%s -> ' "$code"
  curl -s -o /dev/null -w '%{http_code} (%{time_total}s)\n' \
    "https://httpbin.org/status/$code"
done

echo
echo "=== リダイレクト: -L を付けると追いかける ==="
# -w の書式に日本語を入れると Windows の curl では文字化けするので、見出しは printf で出す
printf '追わない: '
curl -s -o /dev/null -w '%{http_code}\n' "https://httpbin.org/redirect/1"
printf '追う:     '
curl -sL -o /dev/null -w '%{http_code}\n' "https://httpbin.org/redirect/1"
