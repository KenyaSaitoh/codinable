#!/usr/bin/env bash
# サーキットブレーカーを OPEN にする（社員APIの停止中に 6 回呼ぶ）
# 前提: ▶ アプリ実行のあと、出力欄の右の「api」の停止で社員API（8095）だけを止めておく（Payara Server は動かしたまま）
# 期待する結果（社員APIの停止中）:
#   6 回とも HTTP/1.1 200 と {"employees":[],"fallback":true,"message":"社員APIを利用できないため代替応答を返しました"}
#   実行結果のログ: 最初の 4 回は [ EmployeeClientService#getEmployees ] のあとに fallbackEmployees が出る（実際に通信して失敗）
#   4 回の結果がそろって失敗率 0.5 以上になると OPEN。5・6 回目は getEmployees のログが出ずに fallbackEmployees だけが出る（遮断）
#   「api」を起動し直し、10 秒以上たってから 2 回呼ぶと（HALF_OPEN で 2 回成功）、CLOSED に戻って社員 16 件が返る
# 社員APIが動いている状態で流すと、6 回とも社員 16 件と "fallback":false が返る
set -x
api=http://localhost:8080/jakarta-fault-tolerance-employee/api
for i in 1 2 3 4 5 6; do
  curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$api/resilience-demo"
  echo
done
