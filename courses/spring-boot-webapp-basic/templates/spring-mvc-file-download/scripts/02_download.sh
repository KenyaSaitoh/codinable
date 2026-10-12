#!/usr/bin/env bash
# sample.txt をダウンロードして、応答ヘッダーと中身を見る
# 先に 01_prepare_sample.sh でファイルを用意し、▶ アプリ実行でアプリを起動しておく（ポート 8080）
# 期待する結果: HTTP/1.1 200、Content-Disposition: attachment; filename="sample.txt"、本文 download sample
set -x
curl -sS -i http://localhost:8080/download/sample.txt
