#!/usr/bin/env bash
# sendEvent（社員登録イベントをRabbitMQに送信する）→ コンシューマーの受信履歴を見る
# 先に ▶ アプリ実行で RabbitMQ・プロデューサー（8093）・コンシューマー（consumer/、8094）を起動しておく
# 期待する結果:
#   1つ目 HTTP/1.1 202（ブローカーが受理した。確認が失敗すると 503）
#   2つ目 HTTP/1.1 200 と、コンシューマーが実際に受信した直近のイベント
# 出力には [consumer] を付けて受信ログも出る。エクスチェンジ employee-exchange やキュー employee-queue の状態は
# 「メッセージング」タブの管理画面（http://127.0.0.1:15672、guest/guest）でも確かめられる
set -x
curl -sS -i -X POST localhost:8093/events -H 'Content-Type: application/json' \
  -d '{"eventType":"EMPLOYEE_CREATED","employeeId":10001,"employeeName":"Alice","occurredAt":"2026-04-01T09:00:00"}'
echo
sleep 2
curl -sS -i localhost:8094/events
