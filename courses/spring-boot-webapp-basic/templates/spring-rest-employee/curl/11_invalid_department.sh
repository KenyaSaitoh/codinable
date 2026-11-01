#!/usr/bin/env bash
# 存在しない部署を指定して登録すると400
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 400 とエラーの内容
set -x
curl -sS -i -X POST http://127.0.0.1:8080/employees \
  -H 'Content-Type: application/json' \
  -d '{"employeeName":"Wendy","departmentId":999,"jobId":1,"salary":230000,"entranceDate":"2018-04-01"}'
