#!/usr/bin/env bash
# ログインせずに社員 API を呼ぶ（認証が無いので401）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8083）
# 期待する結果: HTTP/1.1 401
# ログイン・CSRF トークンのやり取りは、プレビューの React 画面（http://localhost:5173）から確かめる
set -x
curl -sS -i localhost:8083/employees
