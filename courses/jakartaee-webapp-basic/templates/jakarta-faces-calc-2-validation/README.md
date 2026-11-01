# jakarta-faces-calc-2-validation — コンバーターと Bean Validation・FacesMessage

計算アプリケーションの 2 番目の版です。チャプター 6.1「Jakarta Facesの入力検証・変換・例外処理」で、
送信された文字列が `BigDecimal` へ変換され、Bean Validation の制約で検証されてから `CalcBean` に設定される順序と、
入力エラーを項目ごとに再表示する仕組みを確かめます。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/calc/web/CalcBean.java` | `@NotNull`・`@Min(-1000)`・`@Max(1000)` を付けた `BigDecimal` のプロパティと、`add`・`subtract` |
| `src/main/java/pro/kensait/jakartaee/calc/service/CalcService.java` | 足し算と引き算 |
| `src/main/webapp/CalcInputPage.xhtml` | `h:message for="param1"` と `h:messages globalOnly="true"`、入力欄の `label` |
| `src/main/resources/ValidationMessages.properties` | Bean Validation の文言 |
| `src/main/webapp/WEB-INF/faces-config.xml`、`src/main/resources/CalcMessages.properties` | `message-bundle` で Faces 標準の変換エラーの文言を置き換える |
| `curl/01_inputs.sh` | 空欄・文字・範囲外・境界値・正常値を順に送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-calc-2-validation/CalcInputPage.xhtml` が開く
2. 空欄、`abc`、`1001`、`-1000` と `1000`、`30` と `10` などを入れて「足し算」「引き算」を押す
3. `curl/01_inputs.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

| 入力 | 表示 |
|---|---|
| 空欄 | 「値を入力してください」（`@NotNull`） |
| `abc` | 「パラメータ1は数値で入力してください」（`BigDecimal` への変換） |
| `1001`、`-1001` | 「入力可能な最大値は1000」「入力可能な最小値は-1000」（範囲の制約） |
| `-1000` と `1000` | 境界値は範囲内なので計算される |
| `30` と `10` | 足し算 40、引き算 20 |

- エラーのときは、入力した文字（`abc` など）が入力欄に残ったまま同じ画面が再表示される。プロパティには設定されず、`add` も呼ばれない
- エラーは入力欄ごとに、その右の `h:message` に出る
