#!/usr/bin/env bash
# calc/tests.py のテスト（CalcServiceTest・CalcViewTest）を python manage.py test で流す
# 先に一度 ▶ アプリ実行で開発サーバーを起動しておく（そのときに Django が自動で入る）
# 期待する結果: 最後に OK と、実行したテストの件数が出る
cd "$(dirname "$0")" || exit 1
set -x
python manage.py test calc -v 2
