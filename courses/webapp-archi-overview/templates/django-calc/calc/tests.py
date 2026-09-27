"""ターミナルから python manage.py test で実行する。

Spring MVC 版の CalcServiceTest と同じく、まず services を画面抜きで確かめ、
そのあと View を通したときの振る舞いを確かめる。
"""

from django.test import SimpleTestCase
from django.urls import reverse

from . import services


class CalcServiceTest(SimpleTestCase):
    def test_add(self):
        self.assertEqual(services.add(10, 20), 30)

    def test_subtract(self):
        self.assertEqual(services.subtract(10, 20), -10)

    def test_multiply(self):
        self.assertEqual(services.multiply(10, 20), 200)

    def test_divide(self):
        self.assertEqual(services.divide(10, 20), 0.5)

    def test_divide_by_zero(self):
        with self.assertRaises(ValueError):
            services.divide(10, 0)


class CalcViewTest(SimpleTestCase):
    def test_index_displays_form(self):
        response = self.client.get(reverse("calc:index"))
        for label in ["足し算", "引き算", "掛け算", "割り算"]:
            self.assertContains(response, label)

    def test_post_calculates_result(self):
        response = self.client.post(reverse("calc:multiply"), {"param1": "10", "param2": "20"})
        self.assertContains(response, "計算結果 =&gt; 200.0")

    def test_invalid_value_returns_to_input(self):
        response = self.client.post(reverse("calc:add"), {"param1": "abc", "param2": "20"})
        self.assertTemplateUsed(response, "calc/input.html")
        self.assertContains(response, "数値を入力してください")

    def test_out_of_range_returns_to_input(self):
        response = self.client.post(reverse("calc:add"), {"param1": "1001", "param2": "20"})
        self.assertContains(response, "入力可能な最大値は1000")

    def test_divide_by_zero_returns_to_input(self):
        response = self.client.post(reverse("calc:divide"), {"param1": "10", "param2": "0"})
        self.assertTemplateUsed(response, "calc/input.html")
        self.assertContains(response, "0 で割ることはできません")

    def test_get_is_not_allowed_for_calculation(self):
        response = self.client.get(reverse("calc:add"))
        self.assertEqual(response.status_code, 405)
