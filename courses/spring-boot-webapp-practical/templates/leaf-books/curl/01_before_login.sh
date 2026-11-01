#!/usr/bin/env bash
# ログイン前に書籍一覧を呼ぶ（/api/auth/login 以外は認証が必要なので403）
# 先に ▶ アプリ実行で REST API（8080）と Customer Hub（customer-hub/、8081）を起動しておく
# 期待する結果: HTTP/1.1 403
#   Set-Cookie: XSRF-TOKEN=...（更新系の要求で使う CSRF トークン）
set -x
curl -sS -i http://localhost:8080/api/books
