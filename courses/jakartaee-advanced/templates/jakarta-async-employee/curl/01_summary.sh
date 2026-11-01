#!/usr/bin/env bash
# 部署ごとの集計を、3 通りの非同期の書き方で取得する
# 前提: 先に ▶ アプリ実行で jakarta-async-employee をデプロイしておく
# 期待する結果:
#   1つ目 /departments/summary（AsyncResponse）は 200 と 4 部署の集計
#         （PLANNING 3 人・HR 2 人・SALES 5 人・PRODUCT 5 人。部署なしの Victor はどこにも入らない）
#   2つ目 /departments/summary-stage（戻り値の CompletionStage）も同じ結果
#   3つ目 /departments/3/report（@Asynchronous）は SALES の集計 1 件
#   実行結果（Payara Server の出力）に、[ EmployeeSummaryService#summarize ] が部署ごとに出て、thread= に VirtualThread と表示される
set -x
api=http://localhost:8080/jakarta-async-employee/api
curl -sS -i "$api/departments/summary"
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$api/departments/summary-stage"
echo
curl -sS -w '\nHTTP/%{http_version} %{http_code}\n' "$api/departments/3/report"
echo
