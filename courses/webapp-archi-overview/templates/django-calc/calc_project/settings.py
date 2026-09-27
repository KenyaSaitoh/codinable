"""計算アプリケーションに必要な最小限の Django 設定。"""

from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

# 学習用のローカル開発設定。本番では環境変数から安全な値を読み込む
SECRET_KEY = "codinable-django-calc-development-only"
DEBUG = True
ALLOWED_HOSTS = ["127.0.0.1", "localhost"]

INSTALLED_APPS = [
    "calc",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
]

ROOT_URLCONF = "calc_project.urls"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.request",
            ],
        },
    }
]

WSGI_APPLICATION = "calc_project.wsgi.application"

# この演習ではデータベースを使わない (Spring MVC 版・Express 版と同じ)
DATABASES = {}

LANGUAGE_CODE = "ja"
TIME_ZONE = "Asia/Tokyo"
USE_I18N = True
USE_TZ = True
