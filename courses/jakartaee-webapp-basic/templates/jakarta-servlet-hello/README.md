# jakarta-servlet-hello — サーブレットのライフサイクル

チャプター 2.1「Jakarta EEとサーブレットによるHTTP処理」の最初のサンプルです。
`HelloServlet` は `@WebServlet` で `/hello` に対応付けたサーブレットで、`loadOnStartup = 1` により
デプロイの時点で生成され、`init` が呼ばれます。GET のたびに `doGet` が HTML を書き出します。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/hello/HelloServlet.java` | `/hello` を受け持つサーブレット |
| `build.gradle` | `war` プラグインと、コンパイル時だけ使う仕様の API（`compileOnly`） |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。Gradle が WAR を作り、同梱の Payara Server 7 へデプロイする
2. デプロイが終わると、プレビューに `http://localhost:8080/jakarta-servlet-hello/hello` が開き、「Hello, Alice!」が表示される

## 結果のどこを見るか

- 実行結果に、`Loading application [jakarta-servlet-hello] at [/jakarta-servlet-hello]` と
  `jakarta-servlet-hello was successfully deployed` が出る。WAR のファイル名がコンテキストルートになっている
- その少し前に `HelloServlet#init` が出る。最初のリクエストを待たずに、デプロイの時点で `init` が呼ばれている
- ⏹ で止めると Payara Server ごと終了する。もう一度 ▶ アプリ実行を押すと、サーバーを起動し直してデプロイする
