#!/usr/bin/env bash
# 何をするか: 一覧の「削除」で社員 10003（Carol）を削除し、一覧から消えることを確かめる
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく（デプロイし直すと初期データに戻る）
# 期待する結果: 「社員ID 10003 を削除しました」。件数が 1 つ減り、一覧から Carol が消える
#              行は消えずに STATUS が deleted に変わる（論理削除。setStatus だけで、コミット時に UPDATE される）
source "$(dirname "$0")/faces.sh"
jar=$(mktemp)
set -x
open "$jar" EmployeeTablePage.xhtml
post "$jar" searchForm employeeTable:2:deleteButton; messages; summary; rows
