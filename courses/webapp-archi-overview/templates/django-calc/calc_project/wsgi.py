"""本番用WebサーバーからDjangoを呼び出すためのWSGI入口。"""

import os

from django.core.wsgi import get_wsgi_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "calc_project.settings")

application = get_wsgi_application()
