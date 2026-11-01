#!/usr/bin/env bash
# トレースビューアーで trace/BookStoreTest.zip を開く
# 前提: 先に BookStoreTest を ▶ 実行して、トレースを保存しておく。閉じると終わる
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に BookStoreTest を ▶ 実行してください' >&2
  exit 1
fi
set -x
./gradlew.bat showTrace --console=plain
