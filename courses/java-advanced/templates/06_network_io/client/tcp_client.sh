#!/usr/bin/env bash
# TCP クライアント lsn_6_1_3.client.Main を動かす（"Alice" を送り、応答を表示する）
# 前提: 待ち受けるサーバーを先に ファイル実行しておく。サーバーが無いときは接続できずに例外で終わる
# アプリ（サーバー）を止めずに脇で動く。クライアントの .java を ファイル実行したときと違い、サーバーは一緒に起動しない
cd "$(dirname "$0")/.." || exit 1
shopt -s globstar
mkdir -p build/script-classes
javac -encoding UTF-8 -nowarn -d build/script-classes src/main/java/pro/**/*.java || exit 1
set -x
java -cp build/script-classes pro.kensait.java.advanced.lsn_6_1_3.client.Main
