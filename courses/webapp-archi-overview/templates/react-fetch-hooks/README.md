# react-fetch-hooks — Fetch 処理をカスタムフックへ分離する

チャプター 10.1.5 / 10.1.7「useEffect・Fetch・カスタムフック」の演習です。
`useEffect` からローカルの JSON（`public/users.json`）を取得し、loading / error / data を State で管理します。
取得処理はカスタムフック `useUsers` に分け、表示コンポーネントから使います。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `src/hooks/useUsers.ts` | 主役。`useEffect` → `fetch` → `setState`。`AbortController` で取得を中断できるようにしている |
| `src/App.tsx` | 表示側。`useUsers()` の戻り値（users / loading / error / reload）だけを使う |
| `public/users.json` | 取得するデータ（3 件） |

## 動かし方

1. ▶ アプリ実行を押す。初回だけ依存パッケージを自動で入れてから Vite の開発サーバー
   （`http://127.0.0.1:5173`）を起動し、プレビュータブが開く（初回は数分かかることがある）
2. 画面を操作する。ソースを書き換えて保存すると、Vite がすぐに画面へ反映する
3. 終わったら「停止」で止める

## 見どころ

- 「再取得」を押すと `requestId` が変わり、`useEffect` がもう一度走る
- `fetch('/users.json')` の URL を存在しないものに変えると、`HTTP 404` のエラー表示を確かめられる
- 表示側（`App.tsx`）は、データをどう取得するかを知らなくてよい
