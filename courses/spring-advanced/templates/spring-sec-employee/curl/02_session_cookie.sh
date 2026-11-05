#!/usr/bin/env bash
# セッションの Cookie の属性と、ログインでセッション ID が変わること（セッション固定化攻撃の対策）を確かめる
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8083）
# 期待する結果:
#   1つ目 GET /csrf は 200。Set-Cookie: SECURITY_SESSION=...; Path=/; HttpOnly; SameSite=Lax
#   2つ目 POST /login（Bob）は 204。Set-Cookie で別の SECURITY_SESSION が届く
#   3つ目 新しいセッション ID で GET /session は 200（name: Bob）
#   4つ目 ログイン前のセッション ID で GET /session は 401（ログイン前の ID を知っていても使えない）
set -x
api=http://localhost:8083
jar=$(mktemp)
first=$(mktemp)
curl -sS -i -c "$jar" "$api/csrf" | tee "$first"
echo
token=$(sed -nE 's/.*"token":"([^"]+)".*/\1/p' "$first")
old_session=$(sed -nE 's/^Set-Cookie: SECURITY_SESSION=([^;]+).*/\1/p' "$first")
curl -sS -i -b "$jar" -c "$jar" -H "X-CSRF-TOKEN: $token" -d username=Bob -d password=2222 "$api/login"
echo
curl -sS -i -b "$jar" "$api/session"
echo
curl -sS -i -b "SECURITY_SESSION=$old_session" "$api/session"
echo
rm -f "$jar" "$first"
