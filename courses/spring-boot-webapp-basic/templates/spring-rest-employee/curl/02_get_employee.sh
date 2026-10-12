#!/usr/bin/env bash
# 社員を1件取得する。versionを確認する
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 200 と社員 10001（Alice）。version も含まれる
set -x
curl -sS -i http://127.0.0.1:8080/employees/10001
