#!/usr/bin/env bash
# TCP クライアント lsn_6_1_3.client.Main_Multi を動かす（3 つのクライアントが同時に接続する）
# 前提: lsn_6_1_3.server.Main_Multi か lsn_6_1_3.server.Main を先に ファイル実行しておく
# アプリ（サーバー）を止めずに脇で動く。クライアントの .java を ファイル実行したときと違い、サーバーは一緒に起動しない
cd "$(dirname "$0")/.." || exit 1
shopt -s globstar
mkdir -p build/script-classes
javac -encoding UTF-8 -nowarn -d build/script-classes src/main/java/pro/**/*.java || exit 1
set -x
java -cp build/script-classes pro.kensait.java.advanced.lsn_6_1_3.client.Main_Multi
