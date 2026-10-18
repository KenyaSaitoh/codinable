#!/usr/bin/env bash
# 何をするか: 削除ボタンと同じ POST /employees/delete を送り（10003 の Carol）、リダイレクトの応答を見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 303 See Other と Location: …/jakarta-jsp-employee/employees
#              続けて一覧を GET すると Carol が見つからない（件数 0）
#              データはメモリー上にあるので、▶ アプリ実行でデプロイし直すと元に戻る
set -x
curl -s -i -X POST http://localhost:8080/jakarta-jsp-employee/employees/delete -d employeeId=10003 | head -5
curl -s http://localhost:8080/jakarta-jsp-employee/employees | grep -c Carol || true
