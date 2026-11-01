#!/usr/bin/env bash
# ビルドテスト（./gradlew test）を流す。除外の設定が効くので ITCalculator と TimeoutTest は動かない
# 前提: 一度テストクラスを ファイル実行しておく（gradlew.bat が用意される）
# 結果: HTML のテストレポートは build/reports/tests/test/index.html、カバレッジは build/reports/jacoco/test/html/index.html
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先にテストクラスを開いて ファイル実行を押してください' >&2
  exit 1
fi
set -x
./gradlew.bat test --rerun-tasks --console=plain
