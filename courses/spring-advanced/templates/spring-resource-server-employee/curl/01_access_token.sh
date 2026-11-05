#!/usr/bin/env bash
# 教材用の発行者（Keycloak と同じ形のパス）からアクセストークンを取り、社員 API を Bearer トークンで呼ぶ
# トークンの取得には、パスワードグラント（検証用の近道）を使う
# 前提: 先に ▶ アプリ実行で起動しておく（ポート 8097）
# 期待する結果:
#   1つ目 トークンなしの GET /employees は 401
#   2つ目 alice（employee_roles: BASIC）のトークンで GET は 200
#   3つ目 alice のトークンで DELETE /employees/10016 は 403（ADMIN が無い）
#   4つ目 bob（employee_roles: ADMIN）のトークンで DELETE は 204。実行結果に「社員を削除しました ... by=bob sub=...」が出る
#         （2 回目からは社員 10016 を削除済みなので 404。▶ アプリ実行し直すと戻る）
#   5つ目 ID トークンをアクセストークンの代わりに送ると 401（aud に employee-api が無い）
#   6つ目 署名を書き換えたトークンは 401
set -x
api=http://localhost:8097
token_endpoint=$api/realms/learn/protocol/openid-connect/token
token() {   # $1=ユーザー名 $2=パスワード $3=取り出す項目（access_token / id_token）
  curl -sS -d grant_type=password -d "username=$1" -d "password=$2" "$token_endpoint" \
    | sed -E "s/.*\"$3\":\"([^\"]+)\".*/\1/"
}
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' "$api/employees"
alice=$(token alice 1111 access_token)
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -H "Authorization: Bearer $alice" "$api/employees"
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -X DELETE -H "Authorization: Bearer $alice" "$api/employees/10016"
bob=$(token bob 2222 access_token)
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -X DELETE -H "Authorization: Bearer $bob" "$api/employees/10016"
id_token=$(token bob 2222 id_token)
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -H "Authorization: Bearer $id_token" "$api/employees"
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -H "Authorization: Bearer ${bob%?????}AAAAA" "$api/employees"
