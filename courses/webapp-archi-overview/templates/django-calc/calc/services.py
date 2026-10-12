"""計算のビジネスロジック。

画面や HTTP のことは一切知らない (Model にあたる層)。
こうしておくと、View を通さずに単体テストできる。
Spring MVC 版の CalcService、Express 版の calculate() にあたる。
"""

# 計算履歴。メモリ上に持つだけなので、サーバーを再起動すると消え、全利用者で共有される。
# 実際のアプリケーションではデータベースに保存する
_history: list[str] = []


def add(param1: float, param2: float) -> float:
    return _record(param1, "+", param2, param1 + param2)


def subtract(param1: float, param2: float) -> float:
    return _record(param1, "-", param2, param1 - param2)


def multiply(param1: float, param2: float) -> float:
    return _record(param1, "×", param2, param1 * param2)


def divide(param1: float, param2: float) -> float:
    if param2 == 0:
        raise ValueError("0 で割ることはできません")
    return _record(param1, "÷", param2, param1 / param2)


def get_history(limit: int) -> list[str]:
    """新しい順に limit 件までの履歴を返す。"""
    return list(reversed(_history))[:limit]


def _record(param1: float, operator: str, param2: float, result: float) -> float:
    """計算式と結果を履歴に追加し、結果をそのまま返す。"""
    _history.append(f"{param1} {operator} {param2} = {result}")
    return result
