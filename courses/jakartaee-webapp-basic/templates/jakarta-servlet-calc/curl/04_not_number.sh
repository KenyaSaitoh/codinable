#!/usr/bin/env bash
# 何をするか: 数値に変換できない文字列を param1 に入れて POST /add を呼ぶ
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果: Double.parseDouble が NumberFormatException を送出し、コンテナが 500 を返す
#              （実行結果にも例外のスタックトレースが出る）
set -x
curl -sS -o /dev/null -w 'HTTP/%{http_version} %{http_code}\n' -X POST http://localhost:8080/jakarta-servlet-calc/add -d param1=abc -d param2=20
