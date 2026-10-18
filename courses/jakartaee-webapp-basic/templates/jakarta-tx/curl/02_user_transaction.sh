#!/usr/bin/env bash
# 何をするか: /baz に 10,-3,20 を送り、BazBean が UserTransaction で 1 件ずつ begin・commit / rollback する様子を見る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（01 の後に流すと Qux の値はそこから増える）
# 期待する結果: COMMITTED 2 / 3。Qux の COUNT は 10 と 20 だけが足される（-3 の 1 件だけが取り消される）
set -x
curl -s "http://localhost:8080/jakarta-tx/business"
curl -s "http://localhost:8080/jakarta-tx/baz?params=10,-3,20"; echo
curl -s "http://localhost:8080/jakarta-tx/business"
