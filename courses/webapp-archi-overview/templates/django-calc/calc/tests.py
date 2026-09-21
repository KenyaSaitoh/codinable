from django.test import SimpleTestCase
from django.urls import reverse

from .services import add


class CalcServiceTest(SimpleTestCase):
    def test_add(self):
        self.assertEqual(add(10, 20), 30)


class CalcViewTest(SimpleTestCase):
    def test_index_displays_form(self):
        response = self.client.get(reverse("calc:index"))
        self.assertContains(response, "足し算(POST)")

    def test_post_calculates_result(self):
        response = self.client.post(
            reverse("calc:add_by_post"), {"param1": "10", "param2": "20"}
        )
        self.assertContains(response, "30.0")

    def test_invalid_value_returns_bad_request(self):
        response = self.client.post(
            reverse("calc:add_by_post"), {"param1": "abc", "param2": "20"}
        )
        self.assertEqual(response.status_code, 400)
        self.assertContains(response, "数値を入力してください", status_code=400)
