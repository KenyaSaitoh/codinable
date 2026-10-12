#!/usr/bin/env bash
# env（特定のプロパティだけを確認する）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"property":{"source":"Config resource 'class path resource [application.yml]'","value":"jdbc:hsqldb:mem:testdb"},...}
set -x
curl -sS -i localhost:8080/actuator/env/spring.datasource.url
