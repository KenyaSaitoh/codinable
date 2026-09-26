# spring-webflux-employee — WebFlux と SSE

Mono／Flux による社員 CRUD と給与範囲検索、`GET /employees/stream` による SSE（1 秒ごとに社員を送り、最後に `complete` イベントを送る）です。組み込みサーバーは Netty です。データはメモリ内にあり、再起動で初期化されます。外部サービスは不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（WebFlux、8089） | 実行対象 `gradle:bootRun` で「実行」 |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押します。
2. ターミナルで確かめる場合:

   ```bash
   curl -i http://localhost:8089/employees/1
   curl -i "http://localhost:8089/employees/query_by_salary?lowerSalary=300000&upperSalary=400000"
   curl -N http://localhost:8089/employees/stream
   ```

   最後の SSE は 1 秒ごとに `data:` が届き、`complete` イベントで終わります。
3. 専用の React 画面を使う場合は、ターミナルで起動し、プレビューの URL 欄に `http://localhost:5173/` を入れます。配信の開始・停止を画面から操作できます。

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

curl の例は `curlメモ.txt` にもあります（Windows のコマンドプロンプト向けの書き方です。bash では `-d` の JSON を単一引用符で囲みます）。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/flux/repository/EmployeeReactiveRepository.java` … 購読時に評価される Mono／Flux
- `src/main/java/pro/kensait/spring/employee/flux/service/EmployeeFluxService.java` … 空結果・完了をつなぐ演算子
- `src/main/java/pro/kensait/spring/employee/flux/api/EmployeeFluxApi.java` … WebFlux の API と SSE
