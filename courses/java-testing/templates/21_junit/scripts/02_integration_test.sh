#!/usr/bin/env bash
# integrationTest タスクを流す。IT で始まる ITCalculator だけが動く
# 前提: 一度テストクラスを ▶ 実行しておく（gradlew.bat が用意される）
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先にテストクラスを開いて ▶ 実行を押してください' >&2
  exit 1
fi
set -x
./gradlew.bat integrationTest --rerun-tasks --console=plain -i | grep -E 'ITCalculator|BUILD|tests completed'
