#!/usr/bin/env bash
# client/ の Java コンソール（Main_Employee）を動かす
# rx() と CompletionStage で社員の取得・部署の検索を並行して送り、確認用の社員 Walter の登録・更新・削除、
# MicroProfile Rest Client による取得と、削除済みの社員の 404 を順に表示して終わる
# 前提: 先に ▶ アプリ実行で確認用の社員API（jakarta-async-client-employee）をデプロイしておく
# client/ には Gradle Wrapper が無いので、ルートの gradlew.bat（▶ アプリ実行のときに作られる）を使う
# 期待する結果: Summary => Alice / SALES 5、Location => .../employees/10017 など、Deleted => 204、Expected error => 404
cd "$(dirname "$0")" || exit 1
if [ ! -f ../gradlew.bat ]; then
  echo '../gradlew.bat がありません。先に ▶ アプリ実行で社員APIをデプロイしてください' >&2
  exit 1
fi
set -x
../gradlew.bat run --console=plain
