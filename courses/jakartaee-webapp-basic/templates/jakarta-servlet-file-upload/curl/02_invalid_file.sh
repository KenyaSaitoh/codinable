#!/usr/bin/env bash
# 何をするか: 受け付けない形式（.exe）と、空のファイルを送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: FileStorageService が InvalidFileException を送出し、web.xml の error-page が理由を示す画面を返す
#              「この形式のファイルは保存できません」「空のファイルは保存できません」（ステータスは 500）
dir=$(mktemp -d)
printf 'MZ' > "$dir/tool.exe"
: > "$dir/empty.txt"
cd "$dir"   # curl へは相対パスで渡す
set -x
curl -s -w '%{http_code}\n' -F "title=Tool" -F "file=@tool.exe" \
  http://localhost:8080/jakarta-servlet-file-upload/files | grep -E '<p>|^[0-9]{3}$'
curl -s -w '%{http_code}\n' -F "title=Empty" -F "file=@empty.txt" \
  http://localhost:8080/jakarta-servlet-file-upload/files | grep -E '<p>|^[0-9]{3}$'
