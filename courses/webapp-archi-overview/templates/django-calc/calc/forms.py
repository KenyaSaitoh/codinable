"""フォームから送られてくるパラメータ。

フィールドの引数が入力値の決まりを表し、View で is_valid() を呼ぶと
Django が送信値を検査して数値に変換してくれる。
Spring MVC 版の CalcParam (@NotNull / @Min / @Max) と同じ決まりにしてある。
"""

from django import forms

# エラーメッセージは Spring MVC 版の ValidationMessages.properties とそろえる
ERROR_MESSAGES = {
    "required": "値を入力してください",
    "invalid": "数値を入力してください",
    "min_value": "入力可能な最小値は%(limit_value)s",
    "max_value": "入力可能な最大値は%(limit_value)s",
}


class CalcForm(forms.Form):
    param1 = forms.FloatField(label="パラメータ1", min_value=-1000, max_value=1000,
                              error_messages=ERROR_MESSAGES)
    param2 = forms.FloatField(label="パラメータ2", min_value=-1000, max_value=1000,
                              error_messages=ERROR_MESSAGES)
