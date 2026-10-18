#!/usr/bin/env bash
# 再試行（@Retry）の回数と待ち時間を確かめる
# 前提: ▶ アプリ実行のあと、出力欄の右の「api」の停止で社員API（8095）だけを止めておく
# 期待する結果（社員APIの停止中）:
#   HTTP/1.1 200 と {"employees":[],"fallback":true,...}（3 回とも失敗したので代替応答）。応答まで約 2 秒以上かかる
#   実行結果のログ: [ EmployeeClientService#getEmployeesWithRetry ] が約 1 秒間隔で 3 回（初回＋再試行 2 回）出て、
#   最後に [ EmployeeFallbackHandler#handle ] getEmployeesWithRetry => ProcessingException が出る
# 社員APIが動いている状態で流すと、1 回目で成功して社員 16 件と "fallback":false が返る
set -x
curl -sS -i -w '\n(%{time_total}s)\n' http://localhost:8080/jakarta-fault-tolerance-employee/api/retry-demo
