#!/usr/bin/env bash
# バルクヘッドの同時実行数の上限を確かめる
# 社員APIの応答を 1.5 秒遅らせてから、同期版（@Bulkhead(2)）と非同期版（@Asynchronous と waitingTaskQueue = 5）へ 5 件ずつ同時に送る
# 前提: 先に ▶ アプリ実行で jakarta-fault-tolerance-employee と社員API（api/、8095）を起動しておく
# 期待する結果:
#   同期版の 5 件: 2 件が 200（Alice）、残りの 3 件は待たずに 503（同時実行数の上限により呼び出しを制限しました、Retry-After: 1）
#   非同期版の 5 件: 実行中 2 件と待ち行列 3 件に収まるので、5 件とも 200（後の 3 件は前の完了を待つので遅れて返る）
#   最後に社員APIの遅延を 0 に戻す
set -x
api=http://localhost:8080/jakarta-fault-tolerance-employee/api
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -X POST 'http://localhost:8095/admin/delay?ms=1500'
echo
for i in 1 2 3 4 5; do
  curl -sS -o /dev/null -w "bulkhead-demo #$i => HTTP/%{http_version} %{http_code}\n" "$api/bulkhead-demo" &
done
wait
for i in 1 2 3 4 5; do
  curl -sS -o /dev/null -w "bulkhead-async-demo #$i => HTTP/%{http_version} %{http_code} (%{time_total}s)\n" "$api/bulkhead-async-demo" &
done
wait
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' -X POST 'http://localhost:8095/admin/delay?ms=0'
echo
