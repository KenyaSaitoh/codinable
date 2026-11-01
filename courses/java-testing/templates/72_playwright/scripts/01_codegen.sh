#!/usr/bin/env bash
# codegen を起動する。Web ブラウザで操作すると、Java のテストコードの下書きが別の窓に記録される
# 前提: 先に アプリ実行でテストアプリを起動しておく。閉じると終わる
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に アプリ実行でアプリを起動してください' >&2
  exit 1
fi
set -x
./gradlew.bat codegen --console=plain
