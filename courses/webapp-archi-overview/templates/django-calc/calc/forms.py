"""HTTPで届いた文字列を検証し、Pythonの数値へ変換するForm。"""

from django import forms


class CalcForm(forms.Form):
    param1 = forms.FloatField(
        label="パラメータ1", error_messages={"required": "数値を入力してください", "invalid": "数値を入力してください"}
    )
    param2 = forms.FloatField(
        label="パラメータ2", error_messages={"required": "数値を入力してください", "invalid": "数値を入力してください"}
    )
