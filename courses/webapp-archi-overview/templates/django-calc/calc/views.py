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


DEFAULT_LIMIT = 10


def index(request: HttpRequest) -> HttpResponse:
    return render(request, "calc/input.html", {"form": CalcForm()})


def history(request: HttpRequest) -> HttpResponse:
    """計算履歴を表示する。

    計算 (add など) は履歴というサーバーの状態を変えるので POST、
    履歴の表示は取得するだけで状態を変えないので GET にする。
    /history?limit=5 のクエリパラメータは request.GET から取り出す (文字列なので int に変換)。
    """
    try:
        limit = int(request.GET.get("limit", DEFAULT_LIMIT))
    except ValueError:
        limit = DEFAULT_LIMIT
    if limit < 1:
        limit = DEFAULT_LIMIT
    return render(request, "calc/history.html", {"history": services.get_history(limit)})


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
