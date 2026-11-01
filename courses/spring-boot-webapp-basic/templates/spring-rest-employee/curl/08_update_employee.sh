#!/usr/bin/env bash
# 07 で登録した社員（ID=10017、version=0）を更新する
# 繰り返す場合は、直前の登録応答にあるIDとversionへ置き換える
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 200 と、更新後の社員（version は 1 になる）
set -x
curl -sS -i -X PUT http://127.0.0.1:8080/employees/10017 \
  -H 'Content-Type: application/json' \
  -d '{"employeeName":"Walter","departmentId":2,"jobId":2,"salary":260000,"entranceDate":"2018-04-01","version":0}'
