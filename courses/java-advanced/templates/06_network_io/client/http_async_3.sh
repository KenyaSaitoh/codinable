#!/usr/bin/env bash
# HTTP クライアント lsn_6_2_4.httpclient.Main_GET_Async_3 を動かす（サーバーなしで試すためのもの）
# 前提: 右の列の「停止」で server を止めておく。Thread.sleep で 30 秒待ってから終わる
# アプリ（サーバー）を止めずに脇で動く。クライアントの .java を ▶ 実行したときと違い、サーバーは一緒に起動しない
cd "$(dirname "$0")/.." || exit 1
shopt -s globstar
mkdir -p build/script-classes
javac -encoding UTF-8 -nowarn -d build/script-classes src/main/java/pro/**/*.java || exit 1
set -x
java -cp build/script-classes pro.kensait.java.advanced.lsn_6_2_4.httpclient.Main_GET_Async_3
