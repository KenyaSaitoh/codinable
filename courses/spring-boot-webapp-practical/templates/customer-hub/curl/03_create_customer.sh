#!/usr/bin/env bash
# 顧客を登録する
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8081）
# DB はインメモリ。アプリを起動し直すと初期データに戻る
# 期待する結果: 登録された Frank の JSON（同じメールアドレスで 2 回目を送るとエラーになる）
set -x
curl -sS -i -X POST http://localhost:8081/customers/ \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Frank","password":"password","email":"frank@gmail.com","birthday":"2000-01-01","address":"Tokyo"}'
