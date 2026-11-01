# spring-rest-greet — REST API の入力の受け取り方

チャプター 11.1「Spring WebとSpring Data RESTによるREST API」（11.1.1・11.1.2）で使うサンプルです。
`GreetApi`（`@RestController`、`@RequestMapping("/greet")`）が、同じ名前の値を
いろいろな HTTP の場所から受け取ります。

| 要求 | 値の場所 | 受け取り方 |
|---|---|---|
| `GET /greet/hello/Alice` | パス | `@PathVariable` |
| `GET /greet/goodbye/Alice` | パス | `@PathVariable` |
| `GET /greet/morning?name=Alice` | クエリ | `@RequestParam` |
| `POST /greet/afternoon` | フォーム | `@RequestParam` |
| `POST /greet/evening` | JSON 本文 | `@RequestBody`（`GreetingRequest`） |
| `GET /greet/night` | `name` ヘッダー | `@RequestHeader` |

## 動かし方

1. ▶ アプリ実行で起動する
2. 起動したまま、`curl/` のスクリプトを順に開いて、エディタの右上の ▶ 実行で送る

| スクリプト | 値の場所 |
|---|---|
| `curl/01_path_variable.sh` | パス（`/greet/hello/Alice`・`/greet/goodbye/Alice`） |
| `curl/02_query_param.sh` | クエリ（`?name=Alice`） |
| `curl/03_form.sh` | フォーム（`-d 'name=Alice'`） |
| `curl/04_json_body.sh` | JSON 本文（`{"name":"Alice"}`） |
| `curl/05_header.sh` | `name` ヘッダー |

## 主なファイル

- `src/main/java/pro/kensait/spring/greet/rest/api/GreetApi.java` … 値の受け取り方ごとのメソッド
- `src/main/java/pro/kensait/spring/greet/rest/api/GreetingRequest.java` … JSON 本文を受け取る型
- `curl/` … 上の表のスクリプト
