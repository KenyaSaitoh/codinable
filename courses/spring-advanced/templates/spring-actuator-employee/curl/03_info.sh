#!/usr/bin/env bash
# info（application.ymlのinfo.app.*とAppInfoContributorのカスタム情報）
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200
#   {"app":{"name":"spring-actuator-employee","description":"社員管理REST API（Spring Boot Actuator教材）","version":"1.0.0"},"runtime":{"startupTime":"...","javaVersion":"25","availableProcessors":8}}
set -x
curl -sS -i localhost:8080/actuator/info
