#!/usr/bin/env bash
# sendEvent（社員登録イベントをKafkaに送信する）→ コンシューマーの受信履歴を見る
# 先に ▶ アプリ実行で Kafka・プロデューサー（8091）・コンシューマー（consumer/、8092）を起動しておく
# 期待する結果:
#   1つ目 HTTP/1.1 202（Kafka のブローカーが受理した。コンシューマーの処理完了までは保証しない）
#   2つ目 HTTP/1.1 200 と、コンシューマーが実際に受信した直近のイベント
#         （届くまで少し時間がかかることがある。空なら数秒おいてもう一度実行する）
# 出力には [consumer] を付けて受信ログも出る。プレビューの http://localhost:8092/events を再読み込みしても確かめられる
set -x
curl -sS -i -X POST localhost:8091/events -H 'Content-Type: application/json' \
  -d '{"eventType":"EMPLOYEE_CREATED","employeeId":10001,"employeeName":"Alice","occurredAt":"2026-04-01T09:00:00"}'
echo
sleep 2
curl -sS -i localhost:8092/events
