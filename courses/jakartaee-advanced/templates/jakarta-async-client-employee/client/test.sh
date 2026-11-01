#!/usr/bin/env bash
# client/ のテスト（ClientOptionsTest）を流す
# 前提: 社員APIは使わないので、デプロイしていなくても動く。ただし client/ には Gradle Wrapper が無いので、
#       ルートの gradlew.bat（一度 ▶ アプリ実行を押すと作られる）を使う
# 期待する結果: 3 つのテストが PASSED になり、BUILD SUCCESSFUL
cd "$(dirname "$0")" || exit 1
if [ ! -f ../gradlew.bat ]; then
  echo '../gradlew.bat がありません。先に一度 ▶ アプリ実行を押してください' >&2
  exit 1
fi
set -x
../gradlew.bat test --console=plain
