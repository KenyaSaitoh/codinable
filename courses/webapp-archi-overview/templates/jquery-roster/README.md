# jquery-roster — jQuery の名簿管理アプリケーション

チャプター 7.2.1「jQueryとDOM操作ライブラリの時代」の演習です。
原稿の名簿管理アプリケーションを jQuery で書いたもので、一覧の取得は GET、登録は POST、
更新は PUT、削除は DELETE を `$.ajax` で REST API に送ります。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `public/app.js` | 主役。jQuery によるイベント登録・DOM 操作・`$.ajax` |
| `public/index.html` / `public/style.css` | 画面 |
| `server.js` | REST API サーバー（Express）。原稿の JAX-RS 版と同じ URL（`/person_service/persons`）と JSON を返す。読み流してよい |

## 動かし方

1. ▶ アプリ実行を押す。初回だけ依存パッケージ（express / jquery）を自動で入れてから
   `http://localhost:3000` で起動し、プレビュータブが開く
2. 一覧・登録・編集・削除を操作する
3. 終わったら「停止」で止める

データはサーバーのメモリにあり、起動し直すと最初の 2 件（Alice / Bob）に戻ります。

## 見どころ

- 「編集」ボタンの処理。Person のデータはテーブルの `<td>` のテキストとして持っていて、
  ボタンを起点に DOM ツリーをたどって取り出している（DOM が表示とデータ保持を兼ねている）
- チャプター 10.1 の React 版（react-spa）ではデータを state として持つので、この「たどる」処理が要らなくなる
