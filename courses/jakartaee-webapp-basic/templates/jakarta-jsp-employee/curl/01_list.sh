#!/usr/bin/env bash
# 何をするか: GET /employees で社員一覧を取得する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200 と、Alice から Trent までの 16 人の表。Victor の部署名は「（所属なし）」
#              実行結果には AccessLogFilter のログ「GET /jakarta-jsp-employee/employees …ms」が出る
set -x
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' http://localhost:8080/jakarta-jsp-employee/employees | grep -E '<td>' | paste - - - - - | head -20
