# Leaf Books SPA（React + TypeScript + Vite）

Leaf Books の画面です。データは親フォルダの REST API（`http://localhost:8080`）から取得します。

```bash
cd frontend
npm install     # 初回だけ
npm run dev     # http://localhost:5173 で起動
```

表示された URL を Codinable のプレビューの URL 欄に入れて開きます。
先に REST API（親フォルダの「実行」）と Customer Hub（`customer-hub/` をターミナルで起動）を
動かしておいてください。`/api` への要求は `vite.config.ts` の proxy で 8080 へ中継されます。

`npm run build` で `dist/` に本番用のファイルを出力します。

## 構成

```
src/
├── main.tsx / App.tsx   エントリポイントと React Router・Provider の配置
├── pages/               画面（一覧・検索・カート・注文確認・注文完了・履歴・ログイン）
├── components/          Header・Layout・BookCard・PrivateRoute・Toast
├── contexts/            AuthContext（ログイン状態）・CartContext（カート）
├── services/            Axios による API 呼び出し（api.ts に CSRF とエラー処理）
└── types/               API の入出力の型
```
