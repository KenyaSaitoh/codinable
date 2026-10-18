#!/usr/bin/env bash
# 何をするか: 一覧画面を開き、氏名と月給の範囲で検索し、ページを送る。社員コードで編集画面を開く
# 前提: 先に ▶ アプリ実行で WAR をデプロイしておく
# 期待する結果:
#   1. 条件なし: 在籍中の社員 16 人、16 件（1 / 4 ページ）。10001 Alice 〜 10005 Ellen
#   2. 次へ: 10006 Frank 〜 10010 Matilda（PageRequest.ofPage(2, 5, true)）
#   3. 氏名に「a」、月給 300000 〜 450000: Carol・Dave・Oscar・Mallory などの部分一致と範囲の両方を満たす社員
#      （@Query の where status = 'active' and employeeName like :pattern and salary between …）
#   4. 月給の下限 500000 > 上限 100000: 「月給の範囲が正しくありません」
#   5. 社員コード E10002 で開く: Bob の編集画面（@Find の byCode）
source "$(dirname "$0")/faces.sh"
jar=$(mktemp)
set -x
open "$jar" EmployeeTablePage.xhtml; echo "$page" | grep -o '在籍中の社員: [0-9]* 人'; summary; rows
post "$jar" searchForm nextButton; summary; rows
post "$jar" searchForm searchButton keyword=a salaryFrom=300000 salaryTo=450000; summary; rows
post "$jar" searchForm searchButton salaryFrom=500000 salaryTo=100000; messages
open "$jar" EmployeeTablePage.xhtml
post "$jar" codeForm openButton employeeCode=E10002; echo "$page" | grep -oE '<h2>[^<]*</h2>|value="Bob"'
