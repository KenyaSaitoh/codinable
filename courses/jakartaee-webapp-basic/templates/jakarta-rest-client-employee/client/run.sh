#!/usr/bin/env bash
# 何をするか: client/ のクライアント API のコンソール（Main_Employee）を動かす
#             社員の取得・検索・登録・更新・削除と、削除後の 404 を順に呼び出して終わる
# 前提: 先に ▶ アプリ実行で確認用 API（http://localhost:8080/employee-api/api）をデプロイしておく
#       （このスクリプトはアプリの脇で動く）
#       client/ には Gradle Wrapper が無いので、ルートの gradlew.bat（▶ アプリ実行のときに作られる）を使う
# 期待する結果: Employee => … から Expected error => 404 まで出て、BUILD SUCCESSFUL で終わる
cd "$(dirname "$0")" || exit 1
if [ ! -f ../gradlew.bat ]; then
  echo '../gradlew.bat がありません。先に ▶ アプリ実行で確認用 API をデプロイしてください' >&2
  exit 1
fi
set -x
../gradlew.bat run --console=plain
