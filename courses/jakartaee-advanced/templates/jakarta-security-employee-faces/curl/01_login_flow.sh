#!/usr/bin/env bash
# Faces の画面のログインの流れと、protected-views による削除画面の保護を curl でたどる
# （Webブラウザの代わりに、Cookie とビューステートを持ち回る）
# 前提: 先に ▶ アプリ実行で jakarta-security-employee-faces をデプロイしておく
# 期待する結果:
#   1つ目 未ログインで社員一覧を開くと 302 で login.xhtml へリダイレクトされる（useForwardToLogin = false）
#   2つ目 Bob / 2222 でログインすると 302 で元の社員一覧へ戻る
#   3つ目 社員一覧の削除のリンクには jakarta.faces.Token が付いている
#   4つ目 トークンを付けずに削除画面の URL を直接開くと 500（ProtectedViewException）で表示できない
#         （Webブラウザでは、web.xml の error-page で理由を示す画面 errors/protected-view.html が出る）
#   5つ目 社員一覧のリンク（トークン付き）から開くと 200 で削除の確認画面が出る
#   6つ目 Alice（BASIC）でログインし直して同じリンクを開くと 403（web.xml の制約。Faces に届く前にコンテナが拒否する）
set -x
app=http://localhost:8080/jakarta-security-employee-faces
jar=$(mktemp)
page=$(mktemp)

# ログイン画面のビューステートを取り、ユーザー名とパスワードをポストバックする
login() {
  local state
  state=$(curl -sS -c "$jar" -b "$jar" "$app/login.xhtml" \
    | sed -nE 's/.*name="jakarta.faces.ViewState"[^>]*value="([^"]+)".*/\1/p' | head -1)
  curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code} -> %{redirect_url}\n' -c "$jar" -b "$jar" \
    --data-urlencode login=login --data-urlencode "login:username=$1" --data-urlencode "login:password=$2" \
    --data-urlencode "login:submit=ログイン" --data-urlencode "jakarta.faces.ViewState=$state" "$app/login.xhtml"
}

curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code} -> %{redirect_url}\n' -c "$jar" -b "$jar" "$app/employees/list.xhtml"
login Bob 2222
curl -sS -o "$page" -c "$jar" -b "$jar" "$app/employees/list.xhtml"
grep -o 'href="[^"]*delete.xhtml[^"]*"' "$page" | head -2
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "$app/employees/delete.xhtml?id=10016"
link=$(grep -o 'href="[^"]*delete.xhtml[^"]*"' "$page" | head -1 | sed -E 's/href="([^"]*)"/\1/; s/&amp;/\&/g')
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "http://localhost:8080$link" | grep -E '<h1>|^HTTP/'

rm -f "$jar"
login Alice 1111
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -c "$jar" -b "$jar" "http://localhost:8080$link"
rm -f "$jar" "$page"
