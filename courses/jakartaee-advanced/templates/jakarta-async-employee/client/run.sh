#!/usr/bin/env bash
# client/ の SSE の受信クライアント（EmployeeStreamClient）を動かす
# GET /api/employees/stream を SseEventSource で受信し、社員を 1 件ずつ表示して、complete を受け取ったら終わる（約 16 秒）
# 前提: 先に ▶ アプリ実行で jakarta-async-employee をデプロイしておく
# client/ には Gradle Wrapper が無いので、ルートの gradlew.bat（▶ アプリ実行のときに作られる）を使う
# 期待する結果: Received => 10001 Alice 〜 Received => 10016 Trent の 16 行と、Count => 16（complete を受け取りました）
cd "$(dirname "$0")" || exit 1
if [ ! -f ../gradlew.bat ]; then
  echo '../gradlew.bat がありません。先に ▶ アプリ実行で jakarta-async-employee をデプロイしてください' >&2
  exit 1
fi
set -x
../gradlew.bat run --console=plain
