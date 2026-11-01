"""Django の管理コマンドを起動する入口。

Codinable の ▶ アプリ実行から引数なしで呼ばれた場合だけ開発サーバーを起動する。
テストは run_tests.sh (python manage.py test) を開いてエディタの右上の ▶ 実行で流す。
"""

import os
import sys
from pathlib import Path


def main() -> None:
    # Codinable同梱のembeddable版Pythonでも、ローカルパッケージを探索できるようにする
    project_dir = str(Path(__file__).resolve().parent)
    if project_dir not in sys.path:
        sys.path.insert(0, project_dir)

    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "calc_project.settings")

    # Codinable の ▶ アプリ実行は引数なしで呼ぶため、引数なしを runserver として扱う
    if len(sys.argv) == 1:
        sys.argv.extend(["runserver", "127.0.0.1:8001", "--noreload"])

    from django.core.management import execute_from_command_line

    execute_from_command_line(sys.argv)


if __name__ == "__main__":
    main()
