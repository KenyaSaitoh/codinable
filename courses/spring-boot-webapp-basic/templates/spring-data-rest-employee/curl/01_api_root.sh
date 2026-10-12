#!/usr/bin/env bash
# 自動公開された3つのRepositoryを発見する
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8080）
# DB はインメモリ（demo プロファイル）。アプリを起動し直すと初期データに戻る
# 期待する結果: HTTP/1.1 200 と、employees・departments・jobs への _links（HAL 形式）
set -x
curl -sS -i http://localhost:8080/api
