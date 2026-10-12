#!/usr/bin/env bash
# サーキットをOPENに遷移させる
# 先に出力タブの行の右端にある「api停止」で呼び出し先 API（8095）を止めておく（このアプリ 8082 は動かしたまま）
# /resilience-demo を5回呼ぶと、最低呼び出し回数（minimum-number-of-calls: 5）に達した時点で
# 失敗率がしきい値（50%）を超え、サーキットがOPENに遷移する
# 期待する結果:
#   5回とも HTTP/1.1 200 と {"employees":[],"fallback":true,"message":"..."}（代替応答）
#   最後の /resilience-status で OPEN
# 出力のログ: 最初は ResourceAccessException、OPEN 遷移後は CallNotPermittedException
# （OPEN後はAPIを呼び出さず即座に代替応答を返すため、応答も速くなる）
set -x
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-status
