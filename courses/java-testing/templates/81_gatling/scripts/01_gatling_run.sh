#!/usr/bin/env bash
# Gatling のシミュレーション（BookStoreSimulation）を流す。最後まで約 3 分 20 秒かかる（maxDuration 200 秒）
# 前提: 先に アプリ実行で spring-bookstore-mvc と spring-bookstore-rest を起動しておく
# 結果: 実行結果にサマリー、build/reports/gatling/bookstoresimulation-<日時>/index.html に HTML レポートができる
# assertions() の基準を満たさないと、gatlingRun が失敗として終わる
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に アプリ実行でアプリを起動してください' >&2
  exit 1
fi
set -x
curl -sS -o /dev/null -w '%{stderr}HTTP/%{http_version} %{http_code}\n' http://localhost:8080/ || { echo '先に アプリ実行でアプリを起動してください' >&2; exit 1; }
./gradlew.bat gatlingRun --simulation pro.kensait.gatling.bookstore.scenario1.BookStoreSimulation --console=plain
