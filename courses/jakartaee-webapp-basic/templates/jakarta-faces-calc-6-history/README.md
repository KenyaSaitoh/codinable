# jakarta-faces-calc-6-history — ビューパラメータと保存済みデータの再取得

計算アプリケーションの 6 番目の版です。チャプター 7.1「画面遷移とスコープによる状態管理」で、
計算のたびに履歴を保存し、結果画面へは履歴の ID だけを付けてリダイレクトし、結果画面がビューパラメータの ID から
保存済みの履歴を取り直す構成を確かめます。履歴はデータベースではなく、メモリー上の Map にあります。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/calc/` の下にあります。

| ファイル | 内容 |
|---|---|
| `web/CalcBean.java` | `CalcOutputPage?faces-redirect=true&id=…` を返す。業務例外は `CalcErrorPage` へ |
| `src/main/webapp/CalcOutputPage.xhtml`、`web/CalcResultBean.java` | `f:viewParam` で `id` を受け取り、`f:viewAction` の `load` が履歴を検索する。無ければ `CalcNotFoundPage` |
| `service/CalcService.java` | 計算と履歴の作成。保存した履歴の ID を返す |
| `service/CalcDAO.java`、`CalcResult.java`、`CalcType.java` | `@ApplicationScoped` の Map に履歴を保存する DAO と、履歴・演算種別 |
| `src/main/webapp/CalcHistoryPage.xhtml` | 履歴の一覧。ID のリンクは `CalcOutputPage.xhtml?id=…` |
| `curl/01_history.sh` | リダイレクト先の URL・再表示・存在しない ID を確かめるスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-calc-6-history/CalcInputPage.xhtml` が開く
2. 何回か計算し、結果画面で再読み込みする。入力画面の「計算履歴」から過去の結果を開く
3. `curl/01_history.sh` を開いて、エディタの右上の ▶ 実行で流す（デプロイ直後の、履歴が空の状態で流す）

## 結果のどこを見るか

- 結果画面のアドレスは `CalcOutputPage.xhtml?id=3` のような形になる。再読み込みしても、別のタブで開いても同じ結果が出る
  （`jakarta-faces-calc-4-prg` の Flash では、再読み込みすると結果が消えた）
- `id=99` のように存在しない ID は `CalcNotFoundPage`、`id=abc` は変換エラー、`id` が無いと必須のエラーになる
- 掛け算で上限（`100000`）を超えるとエラー画面になるが、`CalcService#multiply` は保存してから上限を判定するので、履歴には結果が残る
- 履歴は `@ApplicationScoped` の `CalcDAO` にあるので、利用者ごとに分かれず、▶ アプリ実行でデプロイし直すと消える
