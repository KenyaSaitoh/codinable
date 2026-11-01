#!/usr/bin/env bash
# Controllerを追加せずPOSTできる。employeeId/code/status/versionは送らない
# 本文は client/employee-create.json（このスクリプトはプロジェクトのルートで動くので、そこからの相対パスで渡す）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果: HTTP/1.1 201 と Location・ETag ヘッダー、登録された社員
set -x
curl -sS -i -X POST http://localhost:8080/api/employees -H 'Content-Type: application/json' \
  --data-binary @client/employee-create.json
