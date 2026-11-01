#!/usr/bin/env bash
# 何をするか: アップロード画面を開き、h:form のマルチパートのポストバックで、テキストファイルと .exe を送る
#             （入力欄の名前は uploadForm:title・uploadForm:file。画面の状態 jakarta.faces.ViewState も送る）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   memo.txt  → 302 で UploadResultPage.xhtml?id=1 へリダイレクトし、結果画面にタイトル・ファイル名・サイズが出る
#   tool.exe  → 同じ画面に「この形式のファイルは保存できません」（InvalidFileException → uploadForm:file のメッセージ）
URL=http://localhost:8080/jakarta-faces-file-upload/FileUploadPage.xhtml
dir=$(mktemp -d)
printf 'hello upload\n' > "$dir/memo.txt"
printf 'MZ' > "$dir/tool.exe"
send() {
  local jar viewState
  jar=$(mktemp)
  viewState=$(curl -sS -c "$jar" "$URL" | sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p')
  curl -sS -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -L -b "$jar" -c "$jar" "$URL" \
    -F "uploadForm=uploadForm" -F "uploadForm:title=Memo" -F "uploadForm:file=@$1" \
    -F "uploadForm:saveButton=x" -F "jakarta.faces.ViewState=$viewState" \
    | grep -E '<dd>|color: red'
}
set -x
send "$dir/memo.txt"
send "$dir/tool.exe"
