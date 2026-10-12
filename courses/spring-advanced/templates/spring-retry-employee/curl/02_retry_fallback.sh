#!/usr/bin/env bash
# retryDemo（リトライを観察する）
# 先に出力タブの行の右端にある「api停止」で呼び出し先 API（8095）を止めておく（このアプリ 8081 は動かしたまま）
# 期待する結果: 約3秒（1秒+2秒の指数バックオフ）待たされた後、HTTP/1.1 200 と
#   {"employees":[],"fallback":true,"message":"..."}（代替応答）が返る
# 出力のログ:
#   [ EmployeeClientService#getEmployees ] 試行 1 回目
#   （1秒待機）
#   [ EmployeeClientService#getEmployees ] 試行 2 回目
#   （2秒待機）
#   [ EmployeeClientService#getEmployees ] 試行 3 回目
#   [ EmployeeClientService#recoverEmployees ] リトライ上限に達したため代替応答を返します => ...
# 「api起動」で API を戻すと、01_retry_demo.sh はまた通常の応答になる
set -x
curl -sS -i localhost:8081/retry-demo
