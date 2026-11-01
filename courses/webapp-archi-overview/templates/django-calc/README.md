# django-calc — Django の計算アプリケーション

チャプター 8.3「Djangoによるアプリケーション構築」の演習です。
Spring MVC 版（spring-mvc-calc）と同じ計算アプリケーション（四則演算・入力値検証・0 除算のエラー・
計算履歴）を Django で書いたものです。

## 主なファイル

| ファイル | 中身 | Spring MVC 版で言うと |
|---|---|---|
| `calc/urls.py` | URLconf（`""` / `add` / `subtract` / `multiply` / `divide` / `history`） | `@GetMapping` / `@PostMapping` |
| `calc/views.py` | View（リクエストを受けて画面を返す） | Controller |
| `calc/forms.py` | Form（入力値の検証） | CalcParam |
| `calc/services.py` | 計算と履歴 | CalcService |
| `calc/templates/calc/*.html` | Template | Thymeleaf |
| `calc/tests.py` | テスト（services と View） | CalcServiceTest |
| `manage.py` | 入口。引数なしで呼ばれると開発サーバー（127.0.0.1:8001）を起動する | |

## 動かし方

1. ▶ アプリ実行を押す。初回だけ依存パッケージ（Django）を自動で準備してから開発サーバーを起動し、
   プレビュータブが開く
2. 計算・入力エラー・0 除算・計算履歴を試す
3. 終わったら「停止」で止める

テストは `run_tests.sh` を開いて、エディタの右上の ▶ 実行で流します（中身は `python manage.py test calc`）。
Django は ▶ アプリ実行のときに入るので、先に一度アプリを起動しておいてください。

## 見どころ

URLconf・View・Form・Template・services の役割を、Spring MVC 版の Controller・CalcParam・
Thymeleaf・CalcService と見比べてください。書き方が違うだけで、分け方は同じです。
