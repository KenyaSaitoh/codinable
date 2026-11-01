#!/usr/bin/env bash
# 保存した Trace（build/reports/playwright/<テストメソッド名>.zip）を Playwright の Trace Viewer で開く
# 先に EmployeePlaywrightTest.java を開いてエディタの右上の ▶ 実行でテストを流し、Trace を作っておく
# 既定では registersEditsAndDeletesEmployee.zip を開く。別の Trace を開くときは下の行に -Ptrace= を足す
#   例: ./gradlew.bat showPlaywrightTrace -Ptrace=build/reports/playwright/failedEditKeepsStoredValuesAndCanBeCorrected.zip
# Trace Viewer のウィンドウを閉じると終わる
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に ▶ アプリ実行か、テストクラスの ▶ 実行で一度 Gradle を動かしてください' >&2
  exit 1
fi
set -x
./gradlew.bat showPlaywrightTrace --console=plain
