#!/usr/bin/env bash
# 何をするか: 入力の選択に使うマスター（部署・役職）を取得する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: どちらも 200 と JSON の配列（部署 1 PLANNING 〜 4 PRODUCT、役職 1 ASSOCIATE 〜 4 MANAGER）
set -x
curl -s -i http://localhost:8080/jakarta-rest-employee/api/departments
echo
curl -s -i http://localhost:8080/jakarta-rest-employee/api/jobs
