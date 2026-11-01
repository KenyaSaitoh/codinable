#!/usr/bin/env bash
# 何をするか: sample.txt と日本語の名前のファイルをダウンロードして、応答ヘッダーと中身を見る
# 前提: 先に 01_prepare_sample.sh でファイルを用意し、▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 200、Content-Type: text/plain、Content-Length、
#   Content-Disposition: attachment; filename="sample.txt"; filename*=UTF-8''sample.txt と本文 download sample
#   日本語の名前は filename="___.txt"（ASCII の代替名）と filename*=UTF-8''%E5%A0%B1…（UTF-8 でエンコードした名前）
set -x
curl -sS -i http://localhost:8080/jakarta-servlet-file-download/download/sample.txt
curl -sS -i http://localhost:8080/jakarta-servlet-file-download/download/%E5%A0%B1%E5%91%8A%E6%9B%B8.txt | grep -E '^HTTP|^Content-Disposition'
