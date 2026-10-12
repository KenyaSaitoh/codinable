# express-calc — Express の計算アプリケーション

チャプター 8.4「Expressによるアプリケーション構築」の演習です。
Spring MVC 版（spring-mvc-calc）・Django 版（django-calc）と同じ計算アプリケーション
（四則演算・入力値検証・0 除算のエラー・計算履歴）を Express + EJS で書いたものです。

## 主なファイル

| ファイル | 中身 |
|---|---|
| `app.js` | 主役。ルーティング（`GET /`・`GET /history`・`POST /add` など）、入力値検証、計算と履歴 |
| `views/input.ejs` / `views/output.ejs` / `views/history.ejs` | EJS のテンプレート（入力・結果・履歴） |
| `package.json` | 依存（express / ejs）と `start` スクリプト |

## 動かし方

1. ▶ アプリ実行を押す。初回だけ依存パッケージを自動で入れてから `http://localhost:3000` で起動し、
   プレビュータブが開く
2. 計算・入力エラー・0 除算・計算履歴を試す
3. 終わったら「停止」で止める

## 見どころ

- `express.urlencoded()` がフォームの本文を `req.body` に展開している（書き忘れると `undefined` になる）
- 「サーバー側で HTML を組み立てる」という点は Spring MVC 版・Django 版と同じで、
  違うのはフレームワークの書き方だけ。3 つを見比べてください
