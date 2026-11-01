# jakarta-websocket-employee — Jakarta WebSocket による社員通知

チャプター 3.1「HTTPクライアントとWebSocketによる通信」の後半の演習です。
WebSocket のエンドポイント（`@ServerEndpoint`）と通知の画面（`src/main/webapp`）が同じ WAR にあります。
画面は `http://localhost:8080/jakarta-websocket-employee/`、接続先は `ws://localhost:8080/jakarta-websocket-employee/ws/employees` です。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/notification/EmployeeNoticeEndpoint.java` | エンドポイント（`@OnOpen`・`@OnMessage`・`@OnClose`・`@OnError`） |
| `src/main/java/pro/kensait/jakartaee/employee/notification/NoticeSessions.java` | 接続の一覧（`@ApplicationScoped`）と、全員への配信・送信元だけへの返却 |
| `src/main/java/pro/kensait/jakartaee/employee/notification/EmployeeNoticeDecoder.java` | 受信した JSON を `EmployeeNotice` に変換するデコーダー（読めなければ `DecodeException`） |
| `src/main/java/pro/kensait/jakartaee/employee/notification/EmployeeNoticeEncoder.java` | `EmployeeNotice` を JSON にするエンコーダー（`NoticeErrorEncoder` も同じ構造） |
| `src/main/java/pro/kensait/jakartaee/employee/notification/EmployeeNotice.java` | 通知のレコードと入力条件（`@Positive`・`@NotBlank`・`@Size`） |
| `src/main/java/pro/kensait/jakartaee/employee/notification/OriginCheckConfigurator.java` | ハンドシェイクの `Origin` の確認（許可するオリジンは `microprofile-config.properties`） |
| `src/main/webapp/notification.js` | 画面の JavaScript（Webブラウザ標準の `WebSocket`） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに通知の画面が開き「接続しました」と出る
2. 「送信」を押すと、サーバーの時刻（`occurredAt`）が付いた通知が「受信した通知」に出る。
   別の Webブラウザでも `http://localhost:8080/jakarta-websocket-employee/` を開くと、一方から送った通知が両方に届く
3. 社員名を空にして送ると、送信元の画面にだけ「社員ID・社員名・通知内容を確認してください」が出る
4. 「切断」を押すと `@OnClose` が呼ばれ、以後の通知はその画面に届かない（自動の再接続はしない）
5. `curl/01_websocket.sh` を開いてエディタの右上の ▶ 実行を押すと、2 本の接続での配信、入力エラー、
   JSON として読めないメッセージ（`DecodeException` → `@OnError`）、別のオリジンからの接続の拒否をまとめて確かめられる

## 結果のどこを見るか

- 実行結果に `[ EmployeeNoticeEndpoint#open ]` と `[ EmployeeNoticeEndpoint#close ]` が出る。接続 1 本ごとにエンドポイントのインスタンスと `Session` が作られる
- 入力エラーは送信元の `Session` だけに返り、他の接続には配信されない
- 画面の通知（最大 100 件）は画面のメモリ上の履歴で、再読み込みすると消える。切断中の通知を取り戻す API は無い
- `Origin` が `http://localhost:8080` と完全に一致しない接続は、`checkOrigin` が `false` を返して確立しない
