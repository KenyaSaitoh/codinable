"""URL と View 関数の対応を宣言する、アプリケーション側の URLconf。

Spring MVC 版では @GetMapping / @PostMapping、Express 版では app.get / app.post が
この役割を持つ。Django では URL と処理の対応を 1 か所にまとめて書く。
"""

from django.urls import path

from . import views

app_name = "calc"

urlpatterns = [
    path("", views.index, name="index"),
    path("add", views.add, name="add"),
    path("subtract", views.subtract, name="subtract"),
    path("multiply", views.multiply, name="multiply"),
    path("divide", views.divide, name="divide"),
]
