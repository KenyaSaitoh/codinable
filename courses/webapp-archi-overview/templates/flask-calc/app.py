"""Flask で書いた計算アプリケーション。

Spring MVC 版とやっていることは同じ。
「URL とメソッドを受け取り、計算し、テンプレートに値を渡す」という
サーバーサイド MVC の骨組みは言語が変わっても変わらない。

    1. ターミナルタブで:  pip install -r requirements.txt
    2. 実行対象で「開いているファイル」を選んで実行
    3. http://127.0.0.1:5000 を検知してプレビュータブが開く
"""

from flask import Flask, render_template, request

app = Flask(__name__)


# ビジネスロジック。Web のことを知らない関数として切り離しておく
def calculate(operator: str, param1: float, param2: float) -> float:
    if operator == "add":
        return param1 + param2
    if operator == "subtract":
        return param1 - param2
    if operator == "multiply":
        return param1 * param2
    if operator == "divide":
        if param2 == 0:
            raise ValueError("0 で割ることはできません")
        return param1 / param2
    raise ValueError(f"不明な演算です: {operator}")


# デコレーターで「この URL とメソッドが来たらこの関数」を宣言する
@app.route("/", methods=["GET"])
def index():
    return render_template("input.html")


@app.route("/calc", methods=["POST"])
def calc():
    # フォームの値は常に文字列で届くので、自分で数値に直す (ここが入力値検証にあたる)
    try:
        param1 = float(request.form.get("param1", ""))
        param2 = float(request.form.get("param2", ""))
    except ValueError:
        return render_template("input.html", error="数値を入力してください"), 400

    try:
        result = calculate(request.form.get("operator", ""), param1, param2)
    except ValueError as e:
        return render_template("input.html", error=str(e)), 400

    return render_template("output.html", param1=param1, param2=param2, result=result)


if __name__ == "__main__":
    # debug=True にするとソースを保存しただけで再読み込みされる
    app.run(host="127.0.0.1", port=5000, debug=True)
