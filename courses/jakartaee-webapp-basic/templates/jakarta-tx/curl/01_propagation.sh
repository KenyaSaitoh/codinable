#!/usr/bin/env bash
# 何をするか: /fuga に param=100 と param=-5 を順に送り、そのたびに BUSINESS テーブルの値を見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと COUNT は 0 に戻る）
# 期待する結果:
#   param=100 → NORMAL END。Bar COUNT=100、Qux COUNT=100
#   param=-5  → ERROR OCCURRED!!!。Bar COUNT=95（REQUIRES_NEW の T2 で確定済み）、Qux COUNT=100（T1 とともに取り消し）
#   実行結果（Payara のログ）には FooBean・BarBean・QuxBean の Start / End の順が出る
set -x
curl -s "http://localhost:8080/jakarta-tx/fuga?param=100"; echo
curl -s "http://localhost:8080/jakarta-tx/business"
curl -s "http://localhost:8080/jakarta-tx/fuga?param=-5"; echo
curl -s "http://localhost:8080/jakarta-tx/business"
