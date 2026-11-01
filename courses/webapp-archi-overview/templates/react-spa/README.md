# react-spa — React の SPA（名簿管理）

チャプター 10.1「ReactjsによるSPA」の総合演習です。
Vite + React + TypeScript で作った名簿管理で、チャプター 3 の静的ページ（static-web）や
チャプター 7.2.1 の jQuery 版（jquery-roster）と同じ題材です。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `src/App.tsx` | 主役。名簿の State を持ち、登録・削除・名前での絞り込み（`useMemo`）を行う |
| `src/PersonForm.tsx` | 入力フォーム。確定したら親から渡された `onAdd` を呼ぶ |
| `src/PersonList.tsx` | 一覧の表示。「削除」で親から渡された `onRemove` を呼ぶ |
| `src/types.ts` | 画面をまたいで使う型（`Person`・`Department`） |
| `src/main.tsx` | `index.html` の `#root` に `App` を描画する入口 |

## 動かし方

1. ▶ アプリ実行を押す。初回だけ依存パッケージを自動で入れてから Vite の開発サーバー
   （`http://127.0.0.1:5173`）を起動し、プレビュータブが開く（初回は数分かかることがある）
2. 画面を操作する。ソースを書き換えて保存すると、Vite がすぐに画面へ反映する
3. 終わったら「停止」で止める

## 見どころ

- DOM を自分で組み立てず、State を書き換えると画面が描き直される
- 配列は書き換えず、新しい配列を作って State に渡している（`push` では React が変化に気づけない）
- 静的ページ版・jQuery 版と見比べ、作り方の違いを確かめてください
