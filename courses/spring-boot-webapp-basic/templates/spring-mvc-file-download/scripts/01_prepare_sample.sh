#!/usr/bin/env bash
# ダウンロードするファイル C:/tmp/output/sample.txt を用意する
# FileDownloadController は C:/tmp/output の下のファイルを返す（場所はコードに直接書かれている）
# アプリを起動する前でも後でも動かせる
set -x
mkdir -p /c/tmp/output
echo "download sample" > /c/tmp/output/sample.txt
ls -l /c/tmp/output
