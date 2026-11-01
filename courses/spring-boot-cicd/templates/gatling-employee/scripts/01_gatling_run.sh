#!/usr/bin/env bash
# Gatling の負荷シナリオ（EmployeeSimulation: 20 人・60 リクエスト）を流す
# 先に ▶ アプリ実行でアプリを起動しておく（http://localhost:8080。起動したまま、このスクリプトを ▶ 実行する）
# 期待する結果:
#   1つ目 アプリの health が {"status":"UP"}
#   2つ目 Gatling の集計（件数・失敗 0 件・p95）と assertions の結果。満たさないと Gradle が失敗で終わる
#   HTML レポートは build/reports/gatling/employeesimulation-<実行日時>/index.html にできる
# 接続先は環境変数 GATLING_BASE_URL で変えられる（既定 http://localhost:8080）
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に ▶ アプリ実行でアプリを起動してください' >&2
  exit 1
fi
set -x
curl -sS http://localhost:8080/actuator/health || { echo; echo '先に ▶ アプリ実行でアプリを起動してください' >&2; exit 1; }
echo
./gradlew.bat gatlingRun --console=plain
