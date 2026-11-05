#!/usr/bin/env bash
# 異動を登録し、コミットしたときだけリスナーが動くことを確かめる
# 前提: 先に ▶ アプリ実行で起動しておく（ポート 8098）
# 期待する結果:
#   1つ目 POST /transfers（Alice を部署 1 へ）は 200 と異動後の社員。メール通知や履歴の記録は待たずに返る
#         実行結果に TransferMailNotifier#notify と TransferHistoryRecorder#record が別の仮想スレッドで出る
#   2つ目 GET /transfers に、異動履歴が 1 件増えている
#   3つ目 存在しない部署 99 への異動（Carol）は 400。イベントを発行したあとのコミットで外部キー制約に違反してロールバックする
#         実行結果に TransferMailNotifier#notify は出ない
#   4つ目 GET /transfers の異動履歴は増えていない
#   5つ目 存在しない社員の異動は 404
set -x
api=http://localhost:8098
curl -sS -i -H 'Content-Type: application/json' -d '{"employeeId":10001,"toDepartmentId":1}' "$api/transfers"
echo
sleep 1
curl -sS -i "$api/transfers"
echo
curl -sS -i -H 'Content-Type: application/json' -d '{"employeeId":10003,"toDepartmentId":99}' "$api/transfers"
echo
sleep 1
curl -sS -i "$api/transfers"
echo
curl -sS -i -H 'Content-Type: application/json' -d '{"employeeId":99999,"toDepartmentId":1}' "$api/transfers"
echo
