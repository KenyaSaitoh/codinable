#!/usr/bin/env bash
# stream（SSEストリーミング。1秒間隔で1件ずつ配信される）
# -N（--no-buffer）を付けると、受信した要素が即座に表示され、
# 「全件揃うのを待たずに順次届く」というリアクティブストリームの動きを体感できる
# 先に ▶ アプリ実行でアプリを起動しておく（ポート 8089）
# 期待する結果: 1秒ごとに次の行が届き、最後に complete イベントで終わる
#   data:{"employeeId":10001,"employeeName":"Alice",...}
#   （1秒後）
#   data:{"employeeId":10002,"employeeName":"Bob",...}
#   ...
set -x
curl -sS -i -N localhost:8089/employees/stream
