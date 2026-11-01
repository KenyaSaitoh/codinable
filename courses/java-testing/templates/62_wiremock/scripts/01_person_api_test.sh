#!/usr/bin/env bash
# チャプター6.1 の PersonApiTest の test_GetPerson() を、起動中のモックサーバーに向けて流す
# 前提: 先に person.rest.test1.WireMockApp を起動しておく（起動したまま、このスクリプトを ファイル実行する）
# ファイル実行でテストクラスを流すとモックサーバーが止まるので、脇で動くスクリプトにしてある
cd "$(dirname "$0")/.." || exit 1
if [ ! -f gradlew.bat ]; then
  echo 'gradlew.bat がありません。先に person.rest.test1.WireMockApp を起動してください' >&2
  exit 1
fi
set -x
./gradlew.bat personApiTest --tests "*PersonApiTest.test_GetPerson" --console=plain
