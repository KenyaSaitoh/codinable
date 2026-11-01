#!/usr/bin/env bash
# loggers（ロガーのログレベル一覧）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"levels":["OFF","ERROR","WARN","INFO","DEBUG","TRACE"],"loggers":{"ROOT":{"configuredLevel":"INFO","effectiveLevel":"INFO"},...}}
set -x
curl -sS -i localhost:8080/actuator/loggers
