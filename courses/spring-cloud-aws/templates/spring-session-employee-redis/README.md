# spring-session-employee-redis

基本編 `spring-mvc-employee-jpa` を基にした社員管理アプリです。
検索・ページング・登録・更新・論理削除を引き継ぎ、入力 → 確認 → 保存の間の値を Spring Session（Redis）で保持します。
JDBC 版（演習「spring-session-employee-jdbc」）とはセッションの保存先だけが異なります。

## Redis について

**Redis は Codinable に含まれていません。** そのため、この演習の既定の実行対象は `gradle:test` です。

- `gradle:test`（Redis 不要）: MockMvc で入力 → 確認 → 保存の画面制御とセッション属性を確かめます
- `gradle:bootRun`（Redis が必要）: 起動と社員一覧（`/employees`）の表示までは Redis なしでも動きます。
  新規登録（`/employees/new`）などセッションに書き込む操作をすると、
  `Failed to connect to localhost:6379` でエラーになります。これは「業務データは RDB、
  セッションは Redis」と保存経路が分かれていることの表れです

Redis を用意できる場合（例: Docker Desktop）は、先に起動してから `gradle:bootRun` を実行します。

```bash
docker run --name spring-session-redis --rm -d -p 127.0.0.1:6379:6379 redis:7
```

接続先は `REDIS_HOST`（既定 localhost）と `REDIS_PORT`（既定 6379）で切り替えます。

## Codinable 向けの調整

- 社員用 RDB は HSQLDB サーバーを使わず、このフォルダの `hsqldb-data/` に置くファイル DB で動かします。
  初回起動時に `src/main/resources/db/` の SQL（`sql/hsqldb/2_*.sql` / `3_*.sql` と同じ内容）で社員 10 件が入り、
  2 回目以降は既存のデータを残します。最初の状態に戻すときは、アプリを止めて `hsqldb-data/` を削除します
- Redis クライアントは Lettuce ではなく **Jedis** にしています。Codinable 同梱の JDK には
  Lettuce が必要とする `jdk.net` モジュールが無いためです（`build.gradle` のコメント参照）。
  `application.yml` のセッション設定（`spring.session.redis.namespace` など）は講座のコードと同じです

## 保存先

| 対象 | 保存方法 |
|---|---|
| 登録済み社員 | JPA から HSQLDB または PostgreSQL の EMPLOYEE へ保存 |
| 入力途中 | EmployeeDraft（内部に EmployeeParam）をセッション属性 employeeDraft へ保持 |
| HTTP セッション | Redis。名前空間 spring:session:employee |

`EmployeeDraft` と `EmployeeParam` は Serializable です。JPA エンティティはセッションへ格納しません。

## 再起動で確認すること（Redis を起動している場合）

1. 新規登録で氏名「Session確認社員」、営業部、一般、月給 300000、入社日 2026-04-01 を入力して「確認へ」を押します
2. 確認画面に値が表示され、社員テーブルにはまだ登録されていないことを確認します
3. 「停止」でアプリだけを止めます。Redis とプレビュー（ブラウザー）は維持します
4. もう一度「実行」を押してアプリを起動します
5. 同じプレビューで `/employees/confirm` を開き、入力が復元されることを確認します
6. 「入力へ戻る」から再確認して「保存」を押し、社員一覧で登録を確認します
7. `/employees/draft` は新規入力へ戻ります。登録済み社員は DB に残ります

保存・キャンセルでは `SessionStatus.setComplete()` で管理対象の属性を解除します。
入力開始トークンは CSRF 対策ではありません。認証・認可・CSRF 対策と同時保存の厳密な排他は教材の対象外です。

## テスト

9 件の MVC テストで、確認前の保存禁止、サーバー側の保存値、戻る操作、不正入力、古いタブ、キャンセル、
期限切れ、ID とバージョンの保持、競合時の入力保持を確認します。
MVC のテストが成功したことだけで、Redis との接続まで成立したとは判断できません。
