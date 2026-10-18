#!/usr/bin/env bash
# 何をするか: 社員 10003 を削除し（論理削除）、もう一度取得する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと初期データに戻る）
# 期待する結果: DELETE は 204（本文なし）。その後の GET は 404（status が deleted の社員は在籍中として扱わない）
set -x
curl -s -i -X DELETE http://localhost:8080/jakarta-rest-employee/api/employees/10003
echo
curl -s -i http://localhost:8080/jakarta-rest-employee/api/employees/10003
