#!/usr/bin/env bash
# Java のクラスから生成されたスキーマを取得する（仕様で定められた /graphql/schema.graphql）
# 前提: 先に ▶ アプリ実行で jakarta-graphql-employee をデプロイしておく
# 期待する結果: type Employee（id: ID!・employeeName: String! など）、type Department（employees: [Employee]）、
#               type Query、input EmployeeInput、type Mutation を含むスキーマの全文
set -x
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' http://localhost:8080/jakarta-graphql-employee/graphql/schema.graphql
