#!/usr/bin/env bash
# employees（全件取得。必要なフィールドだけを指定できる＝オーバーフェッチが起きない）
# エンドポイントは /graphql の1つだけ（単一エンドポイント）。
# すべて「POST + JSONボディ（queryプロパティ）」で送信する点がRESTとの大きな違い。
# クエリが長くなるとcurlでは読みにくいので、GraphiQL（プレビューの URL 欄に http://localhost:8088/graphiql）
# で試すのもおすすめ（補完 Ctrl+Space やスキーマのドキュメントも見られる）。
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8088）
# 期待する結果: HTTP/1.1 200
#   {"data":{"employees":[{"id":"10001","employeeName":"Alice","salary":500000},{"id":"10002","employeeName":"Bob","salary":450000},...]}}
set -x
curl -sS -i -X POST localhost:8088/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ employees { id employeeName salary } }"}'
