"""リクエストを受け取り、services に計算させ、表示するテンプレートを返す。

Spring MVC 版の CalcController にあたる。返すテンプレートの名前
("calc/input.html") が calc/templates/calc/input.html に対応する。
HTML はサーバーで組み立てられ、完成した状態でブラウザーに届く。
"""

from django.http import HttpRequest, HttpResponse
from django.shortcuts import render
from django.views.decorators.http import require_POST

from . import services
from .forms import CalcForm


def index(request: HttpRequest) -> HttpResponse:
    return render(request, "calc/input.html", {"form": CalcForm()})


@require_POST
def add(request: HttpRequest) -> HttpResponse:
    return _calculate(request, services.add)


@require_POST
def subtract(request: HttpRequest) -> HttpResponse:
    return _calculate(request, services.subtract)


@require_POST
def multiply(request: HttpRequest) -> HttpResponse:
    return _calculate(request, services.multiply)


@require_POST
def divide(request: HttpRequest) -> HttpResponse:
    return _calculate(request, services.divide)


def _calculate(request: HttpRequest, operation) -> HttpResponse:
    form = CalcForm(request.POST)
    # 入力値の決まり (forms.py) に反していれば入力画面に戻す
    if not form.is_valid():
        return render(request, "calc/input.html", {"form": form})

    param1 = form.cleaned_data["param1"]
    param2 = form.cleaned_data["param2"]
    try:
        result = operation(param1, param2)
    except ValueError as e:
        # 入力形式は正しいが処理として成立しない場合 (業務エラー) は入力画面に戻す
        return render(request, "calc/input.html", {"form": form, "error": str(e)})
    return render(request, "calc/output.html",
                  {"param1": param1, "param2": param2, "result": result})
