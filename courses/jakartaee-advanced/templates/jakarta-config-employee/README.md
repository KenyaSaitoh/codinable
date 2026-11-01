# jakarta-config-employee — MicroProfile Config と JSON Binding

チャプター 2.1「Jakarta EEアプリケーションの構成・設定・互換性」の演習です。
社員APIの接続先とタイムアウトを MicroProfile Config の設定値として持ち、その結果を JSON で返します。
あわせて、社員を返す `GET /api/employees/{id}` で、JSON Binding（JSON-B）の null・項目の順序・日付の変換を確かめます。
DB は使わず、社員 16 件（10001〜10016。Victor だけ部署なし）をメモリ内に持ちます。

## 見るファイル

| ファイル | 内容 |
|---|---|
| `src/main/resources/META-INF/microprofile-config.properties` | 設定値の既定値（序列値 100）。`%dev.` のプロファイルと `${...}` の式 |
| `src/main/java/pro/kensait/jakartaee/employee/config/EmployeeApiSettings.java` | `@ConfigProperty` で設定値を受け取る CDI Bean |
| `src/main/java/pro/kensait/jakartaee/employee/rest/SettingsResource.java` | 決まった設定値と、値を読んだ設定ソースを返す `GET /api/settings` |
| `src/main/java/pro/kensait/jakartaee/employee/rest/EmployeeTO.java` | 社員のレコード（部署なしを表すため部署の項目は参照型） |
| `src/main/java/pro/kensait/jakartaee/employee/rest/JsonbConfigResolver.java` | `ContextResolver<Jsonb>` による JSON-B の変換設定（`withNullValues(true)` など） |
| `src/test/java/pro/kensait/jakartaee/employee/rest/JsonbConfigResolverTest.java` | 既定の変換と、設定を変えた変換の比較 |
| `build.gradle` | `war` プラグインと、API の `compileOnly` |

## 動かし方

1. ツールバーの ▶ アプリ実行を押す。WAR が同梱の Payara Server 7 にデプロイされ、
   プレビューに `http://localhost:8080/jakarta-config-employee/api/settings` の JSON が開く
2. `curl/` のスクリプトを開いて、エディタの右上の ▶ 実行で順に流す
   - `01_settings.sh` … 設定値（`readTimeoutMs` は 3000、`employeesUrl` は式を展開した値）
   - `02_employee.sh` … Alice と Victor の JSON。Victor は `"departmentId":null` が省略されずに出る
   - `03_not_found.sh` … 存在しない社員の 404
3. `JsonbConfigResolverTest.java` を開いて、エディタの右上の ▶ 実行で流す（Payara Server は使わない）

## 結果のどこを見るか

- `01_settings.sh` の `readTimeoutSource` と `readTimeoutOrdinal` は、値を読んだ設定ソースと序列値（100）を示す。
  `%dev.employee.api.read-timeout-ms` は、プロファイル `mp.config.profile` が `dev` ではないので使われない
- `02_employee.sh` の JSON は、宣言の順ではなく項目名の辞書順に並び、`entranceDate` は `"2012-04-01"` の形になる
- `JsonbConfigResolver` の `@Provider` を外して ▶ アプリ実行し直すと、Victor の `departmentId` と `departmentName` が項目ごと省略される
- `microprofile-config.properties` の `employee.api.url` の行を消して ▶ アプリ実行すると、必須の設定が無いため配置そのものが失敗する
- `employee.api.url` の値はチャプター3.1の社員APIの URL を例として借りているだけで、この演習がそのAPIを呼び出すことはない
