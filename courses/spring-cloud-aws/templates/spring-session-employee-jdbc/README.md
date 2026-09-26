# spring-session-employee-jdbc

基本編 `spring-mvc-employee-jpa` を基にした社員管理アプリです。
検索・ページング・登録・更新・論理削除を引き継ぎ、入力 → 確認 → 保存の間の値を Spring Session（JDBC）で保持します。

## Codinable での起動

実行対象 `gradle:bootRun` のまま「実行」を押し、プレビューで `http://localhost:8080/employees` を開きます。

- DB は HSQLDB サーバーを使わず、このフォルダの `hsqldb-data/` に置くファイル DB で動かします
  （講座リポジトリの `startHsqldb` / `setupHsqldb` は不要です）
- 社員・部署・役職のテーブルとデータは、初回起動時に `src/main/resources/db/` の SQL
  （`sql/hsqldb/2_*.sql` / `3_*.sql` と同じ内容）で作られます。社員 10 件が入ります
- 2 回目以降の起動では既存のテーブルと行を残します（登録した社員は再起動しても消えません）
- Session 用テーブル `SPRING_SESSION` / `SPRING_SESSION_ATTRIBUTES` も同じ DB に自動で作られます
- DB を最初の状態に戻したいときは、アプリを止めてから `hsqldb-data/` フォルダを削除します

インメモリ DB にしていないのは、下の「再起動で確認すること」を Codinable だけで試せるようにするためです。

## 保存先

| 対象 | 保存方法 |
|---|---|
| 登録済み社員 | JPA から HSQLDB または PostgreSQL の EMPLOYEE へ保存 |
| 入力途中 | EmployeeDraft（内部に EmployeeParam）をセッション属性 employeeDraft へ保持 |
| HTTP セッション | 同じデータソースの SPRING_SESSION / SPRING_SESSION_ATTRIBUTES |

`EmployeeDraft` と `EmployeeParam` は Serializable です。JPA エンティティはセッションへ格納しません。
Boot 4 の自動構成を含む `spring-boot-starter-session-jdbc` を使用しています。

## 再起動で確認すること

1. 新規登録で氏名「Session確認社員」、営業部、一般、月給 300000、入社日 2026-04-01 を入力して「確認へ」を押します
2. 確認画面に値が表示され、社員テーブルにはまだ登録されていないことを確認します
3. 「停止」でアプリだけを止めます。プレビュー（ブラウザー）は閉じません
4. もう一度「実行」を押してアプリを起動します
5. 同じプレビューで `/employees/confirm` を開き、入力が復元されることを確認します
6. 「入力へ戻る」から再確認して「保存」を押し、社員一覧で登録を確認します
7. `/employees/draft` は新規入力へ戻ります。登録済み社員は DB に残ります

保存・キャンセルでは `SessionStatus.setComplete()` で管理対象の属性を解除します。
期限切れや入力未開始なら新規入力へ戻します。
一つのセッションに一つの入力フローを保持します。古いタブからの確認・保存は入力開始トークンで検出します。
入力開始トークンは CSRF 対策ではありません。認証・認可・CSRF 対策と同時保存の厳密な排他は教材の対象外です。

## Aurora PostgreSQL へ接続する場合

社員用 RDB は `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、
`SPRING_DATASOURCE_DRIVER_CLASS_NAME` で切り替えます。AWS アカウントと Aurora が必要です
（作成スクリプトは演習「spring-ecs-employee」の `aws-setup/scripts/setup_aurora.py`）。

- 演習専用 DB へ `sql/postgresql` の 3 ファイルを番号順に初回適用し、ドライバーを `org.postgresql.Driver` にします
- 起動時の HSQLDB 用 SQL は流さないよう `SPRING_SQL_INIT_MODE=never` を指定します
- Aurora の URL 例は `jdbc:postgresql://<endpoint>:5432/coursedb?sslmode=require` です
- 初回の Session 用テーブル作成後は `SPRING_SESSION_JDBC_INITIALIZE_SCHEMA=never` にします

環境変数は「実行」ボタンでは渡せないので、ターミナルで設定してから起動します
（`gradlew.bat` は一度「実行」を押すと Codinable がこのフォルダに用意します）。

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://<endpoint>:5432/coursedb?sslmode=require'
export SPRING_DATASOURCE_USERNAME=courseadmin
export SPRING_DATASOURCE_PASSWORD='<作成時のパスワード>'
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
export SPRING_SQL_INIT_MODE=never
./gradlew.bat bootRun
```

## テスト

実行対象で `gradle:test` を選んで「実行」します。

9 件の MVC テストで、確認前の保存禁止、サーバー側の保存値、戻る操作、不正入力、古いタブ、キャンセル、
期限切れ、ID とバージョンの保持、競合時の入力保持を確認します。
テストは MockMvc で画面制御を確かめるもので、外部ストアへの保存は上記の再起動手順で別途確認します。
