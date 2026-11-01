#!/usr/bin/env bash
# 08 と同じPUTをversion=0のまま再実行すると409（更新競合）
# そのあとGETでversion=1になっていることを確認する
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果:
#   1つ目 HTTP/1.1 409
#   2つ目 HTTP/1.1 200 と "version":1
set -x
curl -sS -i -X PUT http://127.0.0.1:8080/employees/10017 \
  -H 'Content-Type: application/json' \
  -d '{"employeeName":"Walter","departmentId":2,"jobId":2,"salary":260000,"entranceDate":"2018-04-01","version":0}'
echo
curl -sS -i http://127.0.0.1:8080/employees/10017
