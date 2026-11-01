#!/usr/bin/env bash
# 確認用の画面（client/src）を http://127.0.0.1:5500 で配信する
# API は 5500 番からのアクセスだけを CORS で許可しているので、このポートで配信する
# 先に ▶ アプリ実行で API を起動しておく（このスクリプトはアプリの脇で動き続ける）
# 動き出したら、プレビューの URL に http://127.0.0.1:5500 を入れて開く
# 止めるときは「停止」（別のスクリプトを ▶ 実行したときも止まる）
cd "$(dirname "$0")" || exit 1
set -x
python -u -m http.server 5500 --bind 127.0.0.1 --directory src
