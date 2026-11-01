# Leaf Books SPA（React + TypeScript + Vite）

Leaf Books の画面です。データは親フォルダの REST API（`http://localhost:8080`）から取得します。

親フォルダで ▶ アプリ実行を押すと、REST API と Customer Hub と一緒にこの画面の開発サーバー
（`npm run dev`、`http://localhost:5173`）も起動し、待ち受けを始めるとプレビューがこの画面に移ります
（親フォルダの `codinable.services.json`。出力には `[frontend]` が付きます）。
依存（`node_modules`）が無いときは、起動の前に `npm install` が自動で走ります。
`/api` への要求は `vite.config.ts` の proxy で 8080 へ中継されます。

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
