#!/usr/bin/env bash
# HTTP クライアント lsn_6_2_3.httpclient.Main_GET_Sync を動かす
# 前提: 相手にしたいサーバー（lsn_6_2_3.httpserver.Main や、応答まで 10 秒待つ lsn_6_2_4.httpserver.Main）を先に ファイル実行しておく
# アプリ（サーバー）を止めずに脇で動く。クライアントの .java を ファイル実行したときと違い、サーバーは一緒に起動しない
cd "$(dirname "$0")/.." || exit 1
shopt -s globstar
mkdir -p build/script-classes
javac -encoding UTF-8 -nowarn -d build/script-classes src/main/java/pro/**/*.java || exit 1
set -x
java -cp build/script-classes pro.kensait.java.advanced.lsn_6_2_3.httpclient.Main_GET_Sync
