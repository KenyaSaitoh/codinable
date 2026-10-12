#!/usr/bin/env bash
# 社員を登録する。201とLocation、採番されたemployeeIdを確認する
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 201
#   Location: .../employees/10017
#   初期状態で最初に登録した社員は employeeId=10017、version=0
#   （もう一度実行すると 10018 が採番される。08・09 は 10017 を前提にしている）
set -x
curl -sS -i -X POST http://127.0.0.1:8080/employees \
  -H 'Content-Type: application/json' \
  -d '{"employeeName":"Walter","departmentId":3,"jobId":1,"salary":230000,"entranceDate":"2018-04-01"}'
