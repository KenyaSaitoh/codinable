#!/usr/bin/env bash
# ログインせずに、認証が要る API を呼ぶ
# JwtAuthFilter は公開パス（書籍・カテゴリ・ログインなど）だけを通し、それ以外は既定で拒否する
# 先に ▶ アプリ実行で書店 API をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200（書籍の一覧は公開パス）
#   2つ目 HTTP/1.1 401（注文履歴は認証が要る）
#   3つ目 HTTP/1.1 401（在庫は認証が要る）
set -x
api=http://localhost:8080/leaf-books-api/api
curl -sS -o /dev/null -w '%{http_code}\n' "$api/books"
curl -sS -i "$api/orders/history"
echo
curl -sS -i "$api/stocks/1"
echo
