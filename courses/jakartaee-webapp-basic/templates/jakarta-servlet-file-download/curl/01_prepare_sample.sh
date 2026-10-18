#!/usr/bin/env bash
# 何をするか: ダウンロードするファイル C:/tmp/output/sample.txt と、日本語の名前のファイルを用意する
#             FileDownloadServlet は C:/tmp/output の下のファイルを返す（場所はコードに直接書かれている）
# 前提: アプリを起動する前でも後でも動かせる
# 期待する結果: C:/tmp/output に 2 つのファイルができる
set -x
mkdir -p C:/tmp/output
echo "download sample" > C:/tmp/output/sample.txt
echo "日本語の名前のファイル" > "C:/tmp/output/$(printf '\xe5\xa0\xb1\xe5\x91\x8a\xe6\x9b\xb8').txt"
ls -l C:/tmp/output
