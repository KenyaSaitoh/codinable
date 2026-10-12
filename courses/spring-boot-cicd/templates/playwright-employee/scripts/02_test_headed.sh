#!/usr/bin/env bash
# ブラウザ（Chromium）を画面に表示して E2E テストを流し、操作の動きを目で見る
# アプリはテストがランダムポートで自動起動するので、▶ アプリ実行で起動しておく必要はない
# --rerun-tasks で、前回成功していてもテストをもう一度流す
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に ▶ アプリ実行か、テストクラスの ▶ 実行で一度 Gradle を動かしてください' >&2
  exit 1
fi
set -x
./gradlew.bat test -Dplaywright.headless=false --rerun-tasks --console=plain
