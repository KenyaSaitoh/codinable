#!/usr/bin/env bash
# 部署・職種もHAL形式の独立したリソース
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果: どちらも HTTP/1.1 200 と _embedded に 4 件
set -x
curl -sS -i 'http://localhost:8080/api/departments?sort=departmentId,asc'
echo
curl -sS -i 'http://localhost:8080/api/jobs?sort=grade,asc'
