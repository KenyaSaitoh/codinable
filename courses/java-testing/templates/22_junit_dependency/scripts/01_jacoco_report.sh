#!/usr/bin/env bash
# テストを流して JaCoCo のレポートを作る
# 前提: 一度テストクラスを ▶ 実行しておく（gradlew.bat が用意される）
# 結果: build/reports/jacoco/test/html/index.html（HTML）と build/reports/jacoco/test/jacocoTestReport.xml（XML）
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先にテストクラスを開いて ▶ 実行を押してください' >&2
  exit 1
fi
set -x
./gradlew.bat test jacocoTestReport --rerun-tasks --console=plain
ls build/reports/jacoco/test/html/index.html build/reports/jacoco/test/jacocoTestReport.xml
