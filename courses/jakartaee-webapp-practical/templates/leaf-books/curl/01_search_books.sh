#!/usr/bin/env bash
# 書籍を検索する（認証は要らない）
# BookDao の JPQL による検索と Criteria API による検索に、同じ条件を渡して結果を比べる
# 先に ▶ アプリ実行で書店 API（/leaf-books-api）と Customer Hub をデプロイしておく
# 期待する結果:
#   1つ目 HTTP/1.1 200 と書籍 1 件（bookId 1）
#   2つ目 JPQL の検索（カテゴリ 1 = Java、キーワード「Java」）
#   3つ目 Criteria API の検索。2つ目と同じ書籍が返る
set -x
api=http://localhost:8080/leaf-books-api/api
curl -sS -i "$api/books/1"
echo
curl -sS "$api/books/search/jpql?categoryId=1&keyword=Java"
echo
curl -sS "$api/books/search/criteria?categoryId=1&keyword=Java"
echo
