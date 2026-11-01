#!/usr/bin/env bash
# loggers（特定パッケージのログレベルを再起動なしでDEBUGに変更する）→ 変更後のレベルを確かめる
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果:
#   1つ目 HTTP/1.1 204
#   2つ目 HTTP/1.1 200
#     {"configuredLevel":"DEBUG","effectiveLevel":"DEBUG"}
set -x
curl -sS -i -X POST localhost:8080/actuator/loggers/pro.kensait.spring.employee -H 'Content-Type: application/json' \
  -d '{"configuredLevel":"DEBUG"}'
echo
curl -sS -i localhost:8080/actuator/loggers/pro.kensait.spring.employee
