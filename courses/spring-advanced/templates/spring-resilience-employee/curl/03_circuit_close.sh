#!/usr/bin/env bash
# HALF_OPEN→CLOSEDへの復帰を確かめる
# 先に出力タブの行の右端にある「api起動」で呼び出し先 API（8095）を起動し直しておく
# OPENから10秒（wait-duration-in-open-state）たってから実行すると、
# HALF_OPENに遷移し、試験呼び出し（3回）が成功するとCLOSEDへ復帰する
# 期待する結果:
#   3回とも HTTP/1.1 200 と "fallback":false の社員リスト
#   最後の /resilience-status で CLOSED（まだ OPEN なら 10 秒おいてもう一度実行する）
set -x
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-demo
echo
curl -sS -i localhost:8082/resilience-status
