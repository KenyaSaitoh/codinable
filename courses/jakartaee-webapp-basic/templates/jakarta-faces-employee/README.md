# jakarta-faces-employee — CDI によるレイヤー設計（メモリー版の社員管理）

メモリー版の社員管理システムです。チャプター 4.1「CDIによるDIとレイヤー設計」で、バッキングBean・Service・DAO が
`@Inject` で接続されたレイヤー構成を読みます。同じアプリケーションを、チャプター 5.1（一覧画面の `h:dataTable`）、
6.1（空欄を null として扱う設定と `@NotEmpty`）、7.1（会話スコープによる入力・確認・確定）でも使います。
社員データはデータベースではなく、`EmployeeDAO` の中の Map にあります。

## 見るファイル

Java のクラスは `src/main/java/pro/kensait/jakartaee/employee/` の下にあります。

| ファイル | 内容 |
|---|---|
| `src/main/webapp/WEB-INF/beans.xml` | `bean-discovery-mode="annotated"`。スコープを付けたクラスだけが CDI Bean になる |
| `web/EmployeeTableBean.java` | 一覧画面のバッキングBean（プレゼンテーション層）。`@PostConstruct` で一覧を取得、`remove` で削除 |
| `web/EmployeeEditBean.java` | 入力画面・確認画面の値を保持する `@ConversationScoped` のバッキングBean（チャプター 7.1） |
| `service/EmployeeService.java` | ビジネス層。`EmployeeDAO` をコンストラクタ注入で受け取る。`protected` の引数なしコンストラクタはクライアントプロキシ用 |
| `service/EmployeeDAO.java`、`service/Employee.java` | データアクセス層。`@PostConstruct` で 16 人の初期データを Map に入れる |
| `src/main/webapp/EmployeeTablePage.xhtml`、`EmployeeInputPage.xhtml`、`EmployeeConfirmPage.xhtml` | 一覧・入力・確認の画面 |
| `src/main/webapp/WEB-INF/web.xml` | `FacesServlet` と、`INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL` |
| `src/test/java/.../service/EmployeeServiceTest.java` | `new EmployeeService(dao)` の形で、コンテナなしに業務処理を確かめるテスト |
| `curl/01_create_flow.sh` | 新規登録の入力・確認・確定を HTTP の要求で順に確かめるスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-faces-employee/EmployeeTablePage.xhtml` が開く
2. 一覧の「新規登録」「編集」「削除」を操作する。入力画面では空欄や範囲外の月給も試す
3. `EmployeeServiceTest.java` を開いて、エディタの右上の ▶ 実行で流す
4. `curl/01_create_flow.sh` を開いて、エディタの右上の ▶ 実行で流す

## 結果のどこを見るか

- 一覧は Alice（10001）から Trent（10016）までの 16 人。部署の無い Victor は「（所属なし）」
- 「新規登録」「編集」で入力画面に移ると、アドレスに `?cid=1` のような会話ID が付く。確認画面から「戻る」と、入力した値が残っている
- 「確定」で一覧へリダイレクトし、会話は終了する。新規登録した社員には 10017 から ID が採番される
- 氏名・部署名を空欄にすると「空要素は許可されていません」（`@NotEmpty`）、月給に `-1` や `10000000` を入れると範囲のエラーが出る
- データはメモリー上にあるので、▶ アプリ実行でデプロイし直すと初期の 16 人に戻る
- テストは、コンテナの外で `EmployeeDAO` と `EmployeeService` を `new` して組み立てている（DI がテストのしやすさにつながる例）
