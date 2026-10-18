#!/usr/bin/env bash
# 何をするか: 存在しないファイルと、../ で保存領域の外（C:/tmp の下や Windows のファイル）を指す要求を送る
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: 404（3 つ目のバックスラッシュ（%5c）を含む要求は、サーブレットに届く前に Payara が 400 で拒否する）。normalize した後のパスが STORAGE の内側（startsWith）でなければ返さない
#              （--path-as-is で、curl が ../ を整理せずにそのまま送る。%2e%2e は .. をエンコードしたもの）
set -x
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/jakarta-servlet-file-download/download/missing.txt
curl -s -o /dev/null -w '%{http_code}\n' --path-as-is "http://localhost:8080/jakarta-servlet-file-download/download/%2e%2e/output2/secret.txt"
curl -s -o /dev/null -w '%{http_code}\n' --path-as-is "http://localhost:8080/jakarta-servlet-file-download/download/..%5c..%5cWindows%5cwin.ini"
