"""計算のビジネスロジック。

画面や HTTP のことは一切知らない (Model にあたる層)。
こうしておくと、View を通さずに単体テストできる。
Spring MVC 版の CalcService、Express 版の calculate() にあたる。
"""


def add(param1: float, param2: float) -> float:
    return param1 + param2


def subtract(param1: float, param2: float) -> float:
    return param1 - param2


def multiply(param1: float, param2: float) -> float:
    return param1 * param2


def divide(param1: float, param2: float) -> float:
    if param2 == 0:
        raise ValueError("0 で割ることはできません")
    return param1 / param2
