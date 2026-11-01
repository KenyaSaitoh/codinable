#!/usr/bin/env bash
# bob のアクセストークンのヘッダーとペイロードを Base64URL から戻して表示する
# （署名付き JWT の中身は誰でも読める。署名は改ざんの検出のためで、秘匿のためではない）
# 前提: 先に ▶ アプリ実行で jakarta-mpjwt-employee と教材用の発行者（issuer/）をデプロイしておく
# 期待する結果: ヘッダーの alg（RS256）・kid と、ペイロードの iss・aud（employee-api を含む）・exp・iat・
#               preferred_username（bob）・groups（["ADMIN"]）・employee_roles が表示される
set -x
token=$(curl -sS -d grant_type=password -d client_id=employee-app -d client_secret=employee-local-secret \
  -d scope=openid -d username=bob -d password=2222 \
  http://localhost:8080/jakarta-mpjwt-issuer/realms/learn/protocol/openid-connect/token \
  | sed -E 's/.*"access_token":"([^"]+)".*/\1/')
node -e '
const [header, payload] = process.argv[1].split(".");
const decode = (part) => JSON.parse(Buffer.from(part, "base64url").toString("utf8"));
console.log("header  =", decode(header));
console.log("payload =", decode(payload));
' "$token"
