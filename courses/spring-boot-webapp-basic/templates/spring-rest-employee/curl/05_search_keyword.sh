#!/usr/bin/env bash
# 氏名の部分一致で検索（日本語などはURLエンコードして送る。--data-urlencode が引き受ける）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ。アプリを起動し直すと初期データ（社員 16 件）に戻る
# 期待する結果: HTTP/1.1 200 と、氏名に Alice を含む社員の一覧
set -x
curl -sS -i --get http://127.0.0.1:8080/employees --data-urlencode 'keyword=Alice'
