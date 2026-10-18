#!/usr/bin/env bash
# 何をするか: 2 人の利用者（別々の Cookie）が同じ社員 10001 の編集画面を開き、順に保存する
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと初期データに戻る）
# 期待する結果:
#   1. 先に保存した利用者 A は「社員ID 10001 を更新しました」（VERSION が 0 → 1）
#   2. 後から保存した利用者 B は「ほかの人が先に更新しました。読み込み直してください」
#      B の画面は VERSION 0 のままなので、UPDATE … WHERE EMPLOYEE_ID = ? AND VERSION = ? の更新件数が 0 になる
source "$(dirname "$0")/faces.inc"
jarA=$(mktemp); jarB=$(mktemp)
set -x
open "$jarA" "EmployeeFormPage.xhtml?employeeId=10001"; pageA=$page
open "$jarB" "EmployeeFormPage.xhtml?employeeId=10001"; pageB=$page
page=$pageA; post "$jarA" employeeForm saveButton employeeName=Alice departmentId=3 jobId=4 salary=510000 entranceDate=2012-04-01; messages
page=$pageB; post "$jarB" employeeForm saveButton employeeName=Alice departmentId=3 jobId=4 salary=520000 entranceDate=2012-04-01; messages
