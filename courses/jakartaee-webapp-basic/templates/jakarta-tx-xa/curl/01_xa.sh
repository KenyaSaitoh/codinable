#!/usr/bin/env bash
# 何をするか: /jta に param=100 と param=-5 を順に送り、そのたびに BUSINESS_XA テーブルの値を見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと COUNT は 0 に戻る）
# 期待する結果:
#   param=100 → NORMAL END。Bar COUNT=100（TxDS1 経由）、Qux COUNT=100（TxDS2 経由）
#   param=-5  → ERROR OCCURRED!!!。Bar・Qux とも COUNT=100 のまま
#              （2 つの XA データソースが FooBean の 1 つのトランザクションに参加し、Qux の例外で両方が取り消される）
set -x
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-tx-xa/jta?param=100"; echo
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-tx-xa/business"
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-tx-xa/jta?param=-5"; echo
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' "http://localhost:8080/jakarta-tx-xa/business"
