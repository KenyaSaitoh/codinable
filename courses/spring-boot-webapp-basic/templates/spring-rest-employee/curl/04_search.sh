#!/usr/bin/env bash
# 部署・役職・月給範囲でAND検索
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 200 と、条件をすべて満たす社員だけの一覧
set -x
curl -sS -i 'http://127.0.0.1:8080/employees?departmentId=3&jobId=1&salaryFrom=250000&salaryTo=300000'
