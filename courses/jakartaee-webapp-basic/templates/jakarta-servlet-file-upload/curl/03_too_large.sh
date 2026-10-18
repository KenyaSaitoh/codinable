#!/usr/bin/env bash
# 何をするか: maxFileSize（5MB）を超える 6MB のファイルを送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: getPart が IllegalStateException を送出し、サーブレットが 413（Content Too Large）を返す
dir=$(mktemp -d)
head -c $((6 * 1024 * 1024)) /dev/zero > "$dir/large.txt"
cd "$dir"   # curl へは相対パスで渡す
set -x
curl -s -o /dev/null -w '%{http_code}\n' -F "title=Large" -F "file=@large.txt" \
  http://localhost:8080/jakarta-servlet-file-upload/files
