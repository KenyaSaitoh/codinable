#!/usr/bin/env bash
# ETagと更新
# 単一GETで得たETagを、If-Matchヘッダーに引用符付きのまま指定する
# 新規作成直後のETagも0とは限らない。必ずレスポンスから取得する
# （PUT・DELETE や 304 の例は client/requests.http にある）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果:
#   1つ目 GET   … HTTP/1.1 200 と ETag: "0" のようなヘッダー
#   2つ目 PATCH … 取得した ETag を If-Match に付けると HTTP/1.1 200（月給が変わり、ETag が 1 つ進む）
#   3つ目 PATCH … 同じ（古くなった）ETag のまま送ると HTTP/1.1 412
set -x
curl -sS -i http://localhost:8080/api/employees/10001
echo
etag=$(curl -s -D - -o /dev/null http://localhost:8080/api/employees/10001 | tr -d '\r' | sed -n 's/^[Ee][Tt][Aa][Gg]: *//p')
curl -sS -i -X PATCH http://localhost:8080/api/employees/10001 -H 'Content-Type: application/json' \
  -H "If-Match: $etag" -d '{"salary":510000}'
echo
curl -sS -i -X PATCH http://localhost:8080/api/employees/10001 -H 'Content-Type: application/json' \
  -H "If-Match: $etag" -d '{"salary":520000}'
