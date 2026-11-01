# jakarta-jsp-employee — サーブレットと JSP による MVC とフィルタ

チャプター 3.1「JSPとMVCパターンによる画面生成」の社員一覧です。
Controller のサーブレット、View の JSP、Model の業務処理のクラスに役割を分け、
すべてのリクエストを `AccessLogFilter` が外側から包みます。
社員データはデータベースではなく、`EmployeeDAO` の中の Map（メモリー上）にあります。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/java/pro/kensait/jakartaee/employee/web/AppInitListener.java` | 起動時に `EmployeeDAO` と `EmployeeService` を作り、`ServletContext` の属性に置くリスナー |
| `src/main/java/pro/kensait/jakartaee/employee/web/EmployeeListServlet.java` | `GET /employees`。一覧をリクエストの属性に置き、JSP へフォワードする |
| `src/main/java/pro/kensait/jakartaee/employee/web/EmployeeDeleteServlet.java` | `POST /employees/delete`。削除してから 303 で一覧へリダイレクトする（PRG） |
| `src/main/java/pro/kensait/jakartaee/employee/web/AccessLogFilter.java` | `/*` のフィルタ。`chain.doFilter` の前後で処理時間を測ってログに出す |
| `src/main/java/pro/kensait/jakartaee/employee/service/EmployeeService.java`、`EmployeeDAO.java`、`Employee.java` | 業務処理と、メモリー上の社員データ |
| `src/main/webapp/WEB-INF/views/employeeList.jsp` | `c:forEach`・`c:out`・`fmt:formatNumber`・`c:if` で表を作る |
| `curl/01_list.sh`、`curl/02_delete_prg.sh` | HTTP の要求を直接送るスクリプト |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。デプロイが終わると、プレビューに `http://localhost:8080/jakarta-jsp-employee/employees` が開く
2. 一覧の「削除」を押す
3. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で順に流す

## 結果のどこを見るか

- 社員は Alice（10001、SALES、500,000）から Trent（10016）までの 16 人。部署を持たない Victor（10014）は「（所属なし）」と出る
- 削除の後、アドレスは `/employees` に変わっている。303 のリダイレクトの後の GET の結果なので、再読み込みしても削除は繰り返されない
- 実行結果に、要求のたびに `AccessLogFilter` のログ（例: `GET /jakarta-jsp-employee/employees 12ms`）が出る。
  削除では `POST …/employees/delete` と、リダイレクト先の `GET …/employees` の 2 行が出る
- データはメモリー上にあるので、▶ アプリ実行でデプロイし直すと削除した社員も元に戻る
