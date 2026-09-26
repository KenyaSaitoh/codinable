# spring-test-employee：社員管理 Web アプリとテスト（チャプター3.1・4.1・8.1）

社員管理システム（Spring MVC + Thymeleaf + JPA）と、層ごとに範囲を絞ったテストです。
DB は組み込みの H2（インメモリ）で、起動のたびに `schema.sql` / `data.sql` で
社員10人・部署4件・役職5件に初期化されます。HSQLDB などを別に起動する必要はありません。

## テスト

| テスト | チャプター | 範囲 | 確かめること |
|---|---|---|---|
| `web/EmployeeParamTest` | 3.1 | 単体 | 文字列入力から業務用の型への変換、必須・境界・形式 |
| `service/EmployeeServiceTest` | 3.1 | 単体（Mockito） | 検索条件の受け渡し、登録・更新・論理削除、古い version の拒否 |
| `EmployeeApplicationIntegrationTest` | 3.1 | 結合（`@SpringBootTest`、`@Tag("it")`） | 実 DB での登録・更新・論理削除と更新拒否 |
| `repository/EmployeeRepositoryTest` | 4.1 | JPA スライス（`@DataJpaTest`、`@Tag("it")`） | 件数・内容・順序、複合条件と範囲の境界、論理削除、flush と DB 制約 |
| `web/EmployeeControllerWebTest` | 4.1 | Web スライス（`@WebMvcTest` + MockMvc） | 画面のモデル、入力エラーと保存の抑止、業務例外から画面への変換 |

## Codinable での動かし方

| 実行対象 | 内容 |
|---|---|
| `test`（既定） | 上の5クラス（単体32件 + `it` タグ8件）をまとめて実行し、テスト結果とカバレッジを表示 |
| `bootRun` | アプリを起動する。プレビューで <http://localhost:8080/employees> を開く |
| `build` | テストを通したうえで実行可能 JAR `build/libs/employee-app.jar` を作る（チャプター8.1） |

元のサンプルは通常の `test` から `@Tag("it")` を除外し、結合テストを別タスク `integrationTest` で
流していました。Codinable の「test」はテスト結果を1つの画面に出すため、ここでは除外をやめて
1回で全部流しています（理由は `build.gradle` のコメント）。原稿・CI と同じ分け方で流したいときは、
一度「実行」を押して `gradlew.bat` ができた後、ターミナルで次を実行します。

```bash
./gradlew.bat integrationTest     # @Tag("it") の結合テストだけ
```

レポートは `build/reports/tests/` に出ます。

## チャプター8.1：CI と成果物

`github/workflows/` に、サンプルリポジトリの `.github/workflows/` にある GitHub Actions の定義を
そのまま収録しています（Codinable のファイル一覧では `.github` が隠れるため、名前を変えて置いています）。

| ファイル | 内容 |
|---|---|
| `github/workflows/ci.yml` | push / pull request / 手動実行で動く CI。`test` ジョブの後に `needs: test` の `e2e` ジョブ。`if: always()` でレポートを保存 |
| `github/workflows/container.yml` | `v*` タグでコンテナーイメージを作成・公開する流れ |
| `github/workflows/sonar.yml` | 手動実行の静的解析（接続先とトークンは GitHub の Secrets から受け取る） |

これらは GitHub 上で動くもので、Codinable の中では実行しません。パスはサンプルリポジトリのルート
（`projects/master/...`）を前提にしており、原稿のとおり実体のない `pipeline-employee` への参照も
残っています。構造（イベント・ジョブの依存・レポート保存・公開条件）を読む教材として扱ってください。

手元では、実行対象 `build` で「テスト → 成果物（`build/libs/employee-app.jar`）」の流れを確かめられます。
作った JAR はターミナルから起動できます（DB はインメモリなので、起動するたびに初期データに戻ります）。

```bash
java -jar build/libs/employee-app.jar
```
