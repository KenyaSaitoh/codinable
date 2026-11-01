# spring-mvc-employee — メモリー版の社員管理

チャプター 2.1「Spring BootとDI・レイヤー設計」で使うサンプルです。
`EmployeeController`（プレゼンテーション層）→ `EmployeeService`（ビジネス層）→
`EmployeeDAO`（データアクセス層）がコンストラクタで接続されています。
保存先は DB ではなく `EmployeeDAO` の中の `ConcurrentHashMap` です（停止すると消えます）。

## 動かし方

1. ツールバーの ▶ アプリ実行で起動する
2. プレビューで `http://localhost:8080/` を開く（社員一覧へ移る）
3. 登録（入力 → 確認 → 確定）・編集・削除を操作する
   - 入力途中の値は `@SessionAttributes("employeeSession")` でセッションに保持される
   - 詳細表示は `/employees/by-query?employeeId=10001`（クエリ）と `/employees/10001`（パス変数）の 2 通り

`src/test/java` の `EmployeeServiceTest.java` と `EmployeeControllerTest.java`（Mockito で依存先を差し替えたテスト）は、
それぞれ開いてエディタの右上の ▶ 実行で流します。

## 主なファイル

- `src/main/java/pro/kensait/spring/employee/web/EmployeeController.java` … 画面遷移と @SessionAttributes
- `src/main/java/pro/kensait/spring/employee/service/EmployeeService.java` … ビジネス層
- `src/main/java/pro/kensait/spring/employee/service/EmployeeDAO.java` … メモリー上の保存先
- `src/test/java/pro/kensait/spring/employee/` … EmployeeServiceTest.java・EmployeeControllerTest.java
