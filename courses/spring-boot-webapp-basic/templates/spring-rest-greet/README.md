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

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. ターミナル（Git Bash）で curl を送る

```bash
curl -i http://localhost:8080/greet/hello/Alice
curl -i "http://localhost:8080/greet/morning?name=Alice"
curl -i -X POST -d "name=Alice" http://localhost:8080/greet/afternoon
curl -i -X POST -H "Content-Type: application/json" -d '{"name":"Alice"}' http://localhost:8080/greet/evening
curl -i -H "name: Alice" http://localhost:8080/greet/night
```
