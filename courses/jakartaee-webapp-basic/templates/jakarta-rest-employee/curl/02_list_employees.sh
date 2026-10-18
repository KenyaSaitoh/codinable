#!/usr/bin/env bash
# 何をするか: 社員の一覧を 1 ページ目と 2 ページ目で取得する（1 ページ 5 件、社員ID 順）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200 と {"content":[…5 件…],"totalElements":16,"totalPages":4}
#              2 ページ目の content は 10006 Frank から
set -x
curl -s http://localhost:8080/jakarta-rest-employee/api/employees
echo
curl -s "http://localhost:8080/jakarta-rest-employee/api/employees?page=2"
