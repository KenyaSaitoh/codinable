# spring-websocket-employee — WebSocket／STOMP による社員通知

Spring の簡易ブローカーを使った STOMP の社員通知です。接続先 `/ws`、送信先 `/app/employees/notifications`、購読先 `/topic/employees`。配信は接続中の利用者だけで、未配信時の保存や再送はありません。

| 場所 | 中身 | 起動方法 |
|---|---|---|
| このフォルダ直下 | Spring Boot（8096） | 実行対象 `gradle:bootRun` で「実行」 |
| `frontend/` | 専用の React 画面（Vite、5173） | ターミナルで `npm run dev` |

## 動かし方

1. 実行対象 `gradle:bootRun` で「実行」を押し、バックエンドを起動します。
2. ターミナルで React 画面を起動します。

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. プレビューの URL 欄に `http://localhost:5173/` を入れて開きます。同じ URL を外部ブラウザでも開き、2 つの画面を接続します。一方から送った通知が両方に届くことを確かめます。

- Vite の表示は `http://127.0.0.1:5173/` ですが、許可 Origin（`FRONTEND_URL`、既定 `http://localhost:5173`）に合わせて **localhost** で開いてください。
- 画面から `/api/websocket/...` への要求は、Vite（`frontend/vite.config.ts`）が 8096 へ転送します。
- 止めるときは、ターミナルで Ctrl+C、バックエンドは「停止」。5173 が使用中だと Vite は起動せずに終了します。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/notification/WebSocketConfig.java` … `/ws` と簡易ブローカーの設定
- `src/main/java/pro/kensait/spring/employee/notification/EmployeeNoticeController.java` … 通知の検証と配信
- `frontend/src/components/NotificationPanel.tsx` … `@stomp/stompjs` による接続・購読・送信
