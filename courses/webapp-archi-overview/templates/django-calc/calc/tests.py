"""ターミナルから python manage.py test で実行する。

Spring MVC 版の CalcServiceTest と同じく、まず services を画面抜きで確かめ、
そのあと View を通したときの振る舞いを確かめる。
"""

from django.test import SimpleTestCase
from django.urls import reverse

from . import services


class CalcServiceTest(SimpleTestCase):
    def setUp(self):
        # 履歴はモジュールの変数に溜まるので、テストごとに空にする
        services._history.clear()

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

    def test_history_newest_first(self):
        services.add(10, 20)
        services.multiply(2, 3)
        self.assertEqual(services.get_history(10), ["2 × 3 = 6", "10 + 20 = 30"])

    def test_history_limit(self):
        for n in range(3):
            services.add(n, n)
        self.assertEqual(services.get_history(2), ["2 + 2 = 4", "1 + 1 = 2"])

    def test_divide_by_zero_not_recorded(self):
        with self.assertRaises(ValueError):
            services.divide(10, 0)
        self.assertEqual(services.get_history(10), [])


class CalcViewTest(SimpleTestCase):
    def setUp(self):
        services._history.clear()

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

    def test_history_shows_newest_first_with_limit(self):
        self.client.post(reverse("calc:add"), {"param1": "10", "param2": "20"})
        self.client.post(reverse("calc:multiply"), {"param1": "2", "param2": "3"})
        response = self.client.get(reverse("calc:history"), {"limit": "1"})
        self.assertContains(response, "2.0 × 3.0 = 6.0")
        self.assertNotContains(response, "10.0 + 20.0 = 30.0")

    def test_history_empty_and_invalid_limit(self):
        response = self.client.get(reverse("calc:history"), {"limit": "abc"})
        self.assertContains(response, "履歴はありません")
