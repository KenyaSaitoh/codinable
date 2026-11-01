#!/usr/bin/env bash
# 既存教材の月給下限検索（こちらは配列）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 200 と、月給 500000 以上の社員の配列
set -x
curl -sS -i 'http://127.0.0.1:8080/employees/query_by_salary?lowerSalary=500000'
