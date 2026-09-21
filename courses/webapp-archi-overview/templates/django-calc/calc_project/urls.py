"""プロジェクト全体の URLconf。"""

from django.urls import include, path

urlpatterns = [
    path("", include("calc.urls")),
]
