#!/usr/bin/env bash
# 原稿・CI と同じ分け方で、@Tag("it") の結合テスト（EmployeeRepositoryTest / EmployeeApplicationIntegrationTest）だけを流す
# 結果は出力とレポート build/reports/tests/integrationTest/index.html で確かめる
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に ▶ アプリ実行か、テストクラスの ▶ 実行で一度 Gradle を動かしてください' >&2
  exit 1
fi
set -x
./gradlew.bat integrationTest --console=plain
