#!/usr/bin/env bash
# ダウンロードするファイル download/sample.txt を用意する
# FileDownloadController はプロジェクト直下の download フォルダのファイルを返す（場所はコードに直接書かれている）
# アプリを起動する前でも後でも動かせる
cd "$(dirname "$0")/.."
set -x
mkdir -p download
echo "download sample" > download/sample.txt
ls -l download
