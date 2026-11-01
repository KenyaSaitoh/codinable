#!/usr/bin/env bash
# 社員一覧（content、totalPages、totalElements）。1ページ5件
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: どちらも HTTP/1.1 200。page=1 と page=2 で content の社員が入れ替わる
set -x
curl -sS -i 'http://127.0.0.1:8080/employees?page=1'
echo
curl -sS -i 'http://127.0.0.1:8080/employees?page=2'
