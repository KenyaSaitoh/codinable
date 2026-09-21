"""URLとView関数の対応を宣言するアプリケーション側URLconf。"""

from django.urls import path

from . import views

app_name = "calc"

urlpatterns = [
    path("", views.index, name="index"),
    path("add/", views.add_by_post, name="add_by_post"),
    path("add-by-get/", views.add_by_get, name="add_by_get"),
]
