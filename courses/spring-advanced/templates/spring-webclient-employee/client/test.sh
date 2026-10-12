#!/usr/bin/env bash
# client/ のテスト（ClientOptionsTest・ConsoleClientTest）を流す
# JDK のローカル HTTP サーバーを相手にするので、確認用 API が起動していなくても動く
# client/ には Gradle Wrapper が無いので、ルートの gradlew.bat（▶ アプリ実行のときに作られる）を使う
# （一度も ▶ アプリ実行を押していないときは、先に押して gradlew.bat を作っておく）
cd "$(dirname "$0")" || exit 1
if [ ! -f ../gradlew.bat ]; then
  echo '../gradlew.bat がありません。先に一度 ▶ アプリ実行を押してください' >&2
  exit 1
fi
set -x
../gradlew.bat test --console=plain
