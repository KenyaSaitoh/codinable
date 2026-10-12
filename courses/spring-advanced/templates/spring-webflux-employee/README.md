# spring-webflux-employee — WebFlux と SSE

Mono／Flux による社員 CRUD と給与範囲検索、`GET /employees/stream` による SSE（1 秒ごとに社員を送り、最後に `complete` イベントを送る）です。組み込みサーバーは Netty です。データはメモリ内にあり、再起動で初期化されます。外部サービスは不要です。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（WebFlux、8089） | ▶ アプリ実行 |
| `frontend/` | 専用の React 画面（Vite、5173） | ▶ アプリ実行で一緒に起動 |

## 動かし方

1. ▶ アプリ実行を押します。バックエンドと React 画面（`frontend/`）が一緒に起動します。`npm install` も必要なときに自動で走ります。
2. curl で確かめる場合は、`curl/` のスクリプトを順に開いて、エディタの右上の ▶ 実行で送ります（アプリは止まりません）。
   `01_get_employee.sh` 〜 `06_remove_employee.sh` が取得・検索・登録・更新・削除（404 を含む）、
   `07_stream.sh` が SSE（`curl -N`）です。SSE は 1 秒ごとに `data:` が届き、`complete` イベントで終わります。
3. React 画面の用意ができると、プレビューに `http://localhost:5173/` が開きます。配信の開始・停止を画面から操作できます。

   React 画面の出力は実行結果タブに `[frontend] ` を付けて出ます。「停止」ではバックエンドだけが止まり、React 画面は出力タブの行の右端にある「frontend停止」「frontend起動」で個別に操作します。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/flux/repository/EmployeeReactiveRepository.java` … 購読時に評価される Mono／Flux
- `src/main/java/pro/kensait/spring/employee/flux/service/EmployeeFluxService.java` … 空結果・完了をつなぐ演算子
- `src/main/java/pro/kensait/spring/employee/flux/api/EmployeeFluxApi.java` … WebFlux の API と SSE
