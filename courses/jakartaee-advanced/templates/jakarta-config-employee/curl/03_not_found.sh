#!/usr/bin/env bash
# 存在しない社員（99999）を取得する
# 前提: 先に ▶ アプリ実行で jakarta-config-employee をデプロイしておく
# 期待する結果: HTTP/1.1 404（EmployeeResource が NotFoundException を投げる）
set -x
curl -sS -i http://localhost:8080/jakarta-config-employee/api/employees/99999
echo
