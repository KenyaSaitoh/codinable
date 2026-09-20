#!/usr/bin/env bash
# GET リクエストを送って、リクエストラインとレスポンスの中身を確かめる。
#
#   実行対象で「開いているファイル」を選んで実行するか、
#   ターミナルタブで bash 01_get.sh と打つ。
set -u

URL="https://httpbin.org/get"

echo "=== 本文だけを見る ==="
curl -s "$URL"

echo
echo "=== ステータス行とレスポンスヘッダーも見る (-i) ==="
curl -si "$URL" | head -20

echo
echo "=== 送信したリクエストも見る (-v は > がリクエスト、< がレスポンス) ==="
curl -sv "$URL" -o /dev/null

echo
echo "=== クエリストリングを付ける ==="
curl -s "$URL?page=1&limit=10"
