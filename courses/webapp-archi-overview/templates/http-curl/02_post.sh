#!/usr/bin/env bash
# POST / PUT / DELETE を送って、メソッドごとの違いを確かめる。
set -u

BASE="https://httpbin.org"

echo "=== フォーム形式 (application/x-www-form-urlencoded) ==="
curl -s -X POST "$BASE/post" \
  -d "param1=10" \
  -d "param2=20"

echo
echo "=== JSON 形式 (application/json) ==="
curl -s -X POST "$BASE/post" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","department":"営業部"}'

echo
echo "=== PUT (置き換え) ==="
curl -s -X PUT "$BASE/put" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","department":"企画部"}'

echo
echo "=== DELETE ==="
curl -si -X DELETE "$BASE/delete" | head -5
