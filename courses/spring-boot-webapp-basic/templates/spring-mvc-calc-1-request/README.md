# spring-mvc-calc-1-request — HTTP の入力と Controller の引数

チャプター 4.1「Spring MVCのリクエスト処理・入力検証・例外処理」の最初のサンプルです。
`GET /` が入力画面、フォームの POST は `/add`、GET による計算は `/addByGet` が受け取ります。
この段階には入力検証がありません。

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. プレビューで `http://localhost:8080/` を開き、足し算(POST) / 足し算(GET) を押す
3. `http://localhost:8080/addByGet?param1=30&param2=10` を直接開き、URL の値が引数に入ることを確かめる

数値以外を入れたり値を空にしたりすると、変換できずにエラーページになります。

## 主なファイル

- `src/main/java/pro/kensait/spring/calc/CalcController.java` … 入力画面・/add・/addByGet
- `src/main/resources/templates/CalcInputPage.html・CalcOutputPage.html` … 入力画面と結果画面
