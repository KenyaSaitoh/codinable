#!/usr/bin/env bash
# client/ の RestClient コンソール（Main_Employee）を動かす
# 社員の取得・検索・登録・更新・削除と、削除後の 404 を順に呼び出して終わる
# 先に ▶ アプリ実行で確認用 API（ポート 8095）を起動しておく（このスクリプトはアプリの脇で動く）
# client/ には Gradle Wrapper が無いので、ルートの gradlew.bat（▶ アプリ実行のときに作られる）を使う
cd "$(dirname "$0")" || exit 1
if [ ! -f ../gradlew.bat ]; then
  echo '../gradlew.bat がありません。先に ▶ アプリ実行で確認用 API を起動してください' >&2
  exit 1
fi
set -x
../gradlew.bat run --console=plain
