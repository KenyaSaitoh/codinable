#!/usr/bin/env bash
# 何をするか: テキストファイルを 1 つ作り、フォームと同じ multipart/form-data で POST /files へ送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 302 で /files/result?id=1 へリダイレクトし（PRG）、結果画面に ID・タイトル・ファイル名・サイズが出る
#              ファイル名は「fakepath/memo.txt」のようにフォルダーを含めて送っても、memo.txt だけが残る
dir=$(mktemp -d)
printf 'hello upload\n' > "$dir/memo.txt"
cd "$dir"   # curl へは相対パスで渡す（Windows の curl は /tmp のようなパスを読めない）
set -x
curl -sS -i -F "title=Memo" -F "file=@memo.txt;filename=fakepath/memo.txt;type=text/plain" \
  http://localhost:8080/jakarta-servlet-file-upload/files | grep -E '^HTTP|^Location'
curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -L -F "title=Memo" -F "file=@memo.txt;filename=fakepath/memo.txt;type=text/plain" \
  http://localhost:8080/jakarta-servlet-file-upload/files | grep -E '<dd>'
