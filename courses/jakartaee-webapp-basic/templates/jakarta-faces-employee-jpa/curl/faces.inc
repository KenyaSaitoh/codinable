# curl/ の各スクリプトが読み込む補助の関数（これ単独では動かさない）
# Faces の画面は、GET で開いた画面のフォームを、画面の状態（jakarta.faces.ViewState）と一緒に POST で送る
BASE=http://localhost:8080/jakarta-faces-employee-jpa
# open <jar> <path>        画面を GET で開き、本文を $page に入れる
open() { page=$(curl -sS -m 30 -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -c "$1" -b "$1" "$BASE/$2"); }
# post <jar> <form> <button> [name=value ...]   $page のフォームを送り、応答（リダイレクトは追う）を $page に入れる
post() {
  local jar=$1 form=$2 button=$3; shift 3
  local action viewState args=()
  action=$(echo "$page" | sed -n "s/.*<form id=\"$form\"[^>]*action=\"\([^\"]*\)\".*/\1/p" | sed 's/&amp;/\&/g')
  viewState=$(echo "$page" | sed -n 's/.*name="jakarta.faces.ViewState"[^>]*value="\([^"]*\)".*/\1/p' | head -1)
  for kv in "$@"; do args+=(--data-urlencode "$form:$kv"); done
  page=$(curl -sS -m 30 -w '%{stderr}HTTP/%{http_version} %{http_code}\n' -L -c "$jar" -b "$jar" "http://localhost:8080$action" \
    --data-urlencode "$form=$form" "${args[@]}" \
    --data-urlencode "$form:$button=x" --data-urlencode "jakarta.faces.ViewState=$viewState")
}
# rows    $page の表を 1 行ずつ、タグを除いて出す
rows() { echo "$page" | tr -d '\n' | sed 's#</tr>#\n#g' | grep '<td>' | sed 's/<[^>]*>/ /g; s/  */ /g; s/編集//; s/^ //'; }
# messages    $page のメッセージ（一覧・項目のエラー）を出す
messages() { echo "$page" | grep -oE '<li[^>]*>[^<]*</li>|<span style="color: red">[^<]*</span>|<span class="[^"]*">[^<]*</span>' | sed 's/<[^>]*>//g'; }
summary() { echo "$page" | grep -oE '[0-9]+ 件（[^）]*）'; }
