#!/usr/bin/env bash
# 部署ごとの集計を、2 通りの非同期の書き方で取得する
# 前提: 先に ▶ アプリ実行で起動しておく（ポート 8098）
# 期待する結果:
#   1つ目 /departments/summary（supplyAsync と allOf）は 200 と 4 部署の集計
#         （PLANNING 3 人・HR 2 人・SALES 5 人・PRODUCT 5 人。部署なしの Victor はどこにも入らない）
#   2つ目 /departments/3/report（@Async）は SALES の集計 1 件
#   実行結果に [ EmployeeSummaryService#summarize ] が部署ごとに出て、thread= に VirtualThread と表示される
set -x
api=http://localhost:8098
curl -sS -i "$api/departments/summary"
echo
curl -sS -i "$api/departments/3/report"
echo
