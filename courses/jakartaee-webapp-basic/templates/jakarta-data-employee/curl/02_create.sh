#!/usr/bin/env bash
# 何をするか: 登録画面から社員を 1 人登録する（入力エラー → 正しい入力）
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと初期データに戻る）
# 期待する結果:
#   1. 氏名が空欄・役職なし・月給 -1 → 項目ごとのエラー
#   2. 正しく入力 → 一覧へリダイレクトし「社員ID 10017（E10017）を登録しました」
#      insert の戻り値の社員 ID から社員コードを作り、update で保存する（同じトランザクション）
source "$(dirname "$0")/faces.sh"
jar=$(mktemp)
set -x
open "$jar" EmployeeFormPage.xhtml
post "$jar" employeeForm saveButton employeeName= departmentId=3 jobId= salary=-1 entranceDate=2026-04-01; messages
open "$jar" EmployeeFormPage.xhtml
post "$jar" employeeForm saveButton employeeName=Zoe departmentId=3 jobId=1 salary=260000 entranceDate=2026-04-01; messages
post "$jar" searchForm searchButton keyword=Zoe; rows
