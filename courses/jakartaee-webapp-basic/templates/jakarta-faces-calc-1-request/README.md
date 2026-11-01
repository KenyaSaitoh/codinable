# jakarta-faces-calc-1-request — フォームとビューパラメータ

計算アプリケーションの最初の版です。チャプター 5.1「Jakarta Facesによる画面生成」でフォームとコンポーネントの値の対応を、
チャプター 6.1「Jakarta Facesの入力検証・変換・例外処理」でビューパラメータ（GET のクエリの受け取り）を確かめます。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/webapp/CalcInputPage.xhtml` | `h:form`・`h:inputText`・`h:commandButton`。同じ画面へ POST で送る（ポストバック） |
| `src/main/java/pro/kensait/jakartaee/calc/web/CalcBean.java` | `Double` のプロパティと、アクションメソッド `add`・`addByGet` |
| `src/main/webapp/CalcOutputPage.xhtml` | `add` が返すアウトカム `"CalcOutputPage"` の画面 |
| `src/main/webapp/CalcAddByGetPage.xhtml` | `f:viewParam` と `f:viewAction` で GET のクエリを受け取って計算する画面 |
| `curl/01_add_by_get.sh` 〜 `curl/03_postback.sh` | HTTP の要求を直接送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-calc-1-request/CalcInputPage.xhtml` が開く
2. 2 つの数値を入れて「足し算」を押す。入力画面の下のリンクで `CalcAddByGetPage.xhtml?param1=30&param2=10` も開く
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で 1 つずつ流す

## 結果のどこを見るか

- 「足し算」の後も、アドレスは `CalcInputPage.xhtml` のまま。アウトカムに `faces-redirect=true` を付けていないので、
  結果画面は同じリクエストの中でレンダリングされる
- `03_postback.sh` で、入力欄の名前が `calcForm:param1` のようなクライアントIDになっていることが分かる
- `CalcAddByGetPage.xhtml?param1=30&param2=10` は 40.0 を表示する。`param1=abc` や `param2` の欠けたクエリでは、
  `h:messages` にエラーが出て `addByGet` は呼ばれない（`02_add_by_get_invalid.sh`）
- この版には入力エラーの扱いが無い。入力画面で空欄のまま「足し算」を押すと、`param1` が null のまま足し算して
  例外の画面になる（入力検証はチャプター 6.1 の `jakarta-faces-calc-2-validation` で扱う）
