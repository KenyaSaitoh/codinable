# spring-mvc-employee — メモリー版の社員管理

チャプター 2.1「Spring BootとDI・レイヤー設計」で使うサンプルです。
`EmployeeController`（プレゼンテーション層）→ `EmployeeService`（ビジネス層）→
`EmployeeDAO`（データアクセス層）がコンストラクタで接続されています。
保存先は DB ではなく `EmployeeDAO` の中の `ConcurrentHashMap` です（停止すると消えます）。

## 動かし方

1. 実行対象で `gradle:bootRun` を選び、「実行」を押す
2. プレビューで `http://localhost:8080/` を開く（社員一覧へ移る）
3. 登録（入力 → 確認 → 確定）・編集・削除を操作する
   - 入力途中の値は `@SessionAttributes("employeeSession")` でセッションに保持される
   - 詳細表示は `/employees/by-query?employeeId=1`（クエリ）と `/employees/1`（パス変数）の 2 通り

実行対象を `gradle:test` にすると、`EmployeeServiceTest` と `EmployeeControllerTest`
（Mockito で依存先を差し替えたテスト）が走ります。
