#!/usr/bin/env bash
# 何をするか: 一覧画面を開き、条件を変えて検索し、ページを送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   1. 条件なし: 16 件（1 / 4 ページ）。ID 順に 10001 Alice 〜 10005 Ellen の 5 件
#   2. 次へ: 10006 Frank 〜 10010 Matilda
#   3. 部署 SALES（3）と月給 300000 以上: Alice・Dave・Ellen の 3 件
#   4. 月給の下限 500000 > 上限 100000: 「月給の範囲が正しくありません」（RangeException）
#   実行結果（Payara のログ）に、EclipseLink が JPQL から作った SELECT が出る
source "$(dirname "$0")/faces.inc"
jar=$(mktemp)
set -x
open "$jar" EmployeeTablePage.xhtml; summary; rows
post "$jar" searchForm nextButton; summary; rows
post "$jar" searchForm searchButton departmentId=3 salaryFrom=300000; summary; rows
post "$jar" searchForm searchButton salaryFrom=500000 salaryTo=100000; messages
