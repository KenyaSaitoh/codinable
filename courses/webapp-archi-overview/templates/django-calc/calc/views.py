"""リクエストを受け、Form・Service・Templateを結び付けるView。"""

from django.http import HttpRequest, HttpResponse, HttpResponseNotAllowed
from django.shortcuts import render

from .forms import CalcForm
from .services import add


def index(request: HttpRequest) -> HttpResponse:
    return render(request, "calc/input.html", {"form": CalcForm()})


def add_by_post(request: HttpRequest) -> HttpResponse:
    if request.method != "POST":
        return HttpResponseNotAllowed(["POST"])
    return _calculate(request, request.POST)


def add_by_get(request: HttpRequest) -> HttpResponse:
    return _calculate(request, request.GET)


def _calculate(request: HttpRequest, data) -> HttpResponse:
    form = CalcForm(data)
    if not form.is_valid():
        return render(request, "calc/input.html", {"form": form}, status=400)

    param1 = form.cleaned_data["param1"]
    param2 = form.cleaned_data["param2"]
    result = add(param1, param2)
    return render(
        request,
        "calc/output.html",
        {"param1": param1, "param2": param2, "result": result},
    )
