# spring-cloud-config — Spring Cloud Config / Context

Config Server が設定を配信し、Config Client が取り込んだ値を `/greeting` で返すサンプルです。
設定がどの経路で取得され、いつ応答へ反映されるか（`@RefreshScope` と `POST /actuator/refresh`）を確かめます。

## 構成

講座リポジトリでは `server/` と `client/` の 2 プロジェクトでしたが、Codinable の「実行」は同時に 1 本だけなので、
**このフォルダ直下 = Config Server**、**`client/` = Config Client（独立した Gradle プロジェクト）** にしています。

| 配置 | ポート | 役割 | 起動方法 |
|---|---|---|---|
| このフォルダ直下 | 8888 | 設定を配信する Config Server | 「実行」（`gradle:bootRun`） |
| `client/` | 8080 | Config Server から設定を取得するクライアント | ターミナルで `../gradlew.bat bootRun` |

Config Server は `native` プロファイルで動作し、クラスパス上の `config-repo/` から設定ファイルを配信します。

- `src/main/resources/config-repo/config-client.yml` … デフォルトプロファイル用
- `src/main/resources/config-repo/config-client-prod.yml` … prod プロファイル用

ファイル名の `config-client` は、クライアント側の `spring.application.name` と一致させることで配信対象になります。

## 起動順と動作確認

必ず **Config Server → Config Client** の順に起動します。

1. 「実行」を押して Config Server を起動します（`Started Application` が出るまで待つ）
2. ターミナルを開き、クライアントを起動します

   ```bash
   cd client
   ../gradlew.bat bootRun
   ```

   `gradlew.bat` は手順 1 で「実行」を押したときに Codinable がこのフォルダ直下に用意します。
   見つからないと言われたら、先に手順 1 を済ませてください。
3. もう 1 つターミナルを開き（またはプレビューの URL 欄で）確認します

   ```bash
   # Config Server が配信する設定を直接確認する（/{アプリケーション名}/{プロファイル}）
   curl localhost:8888/config-client/default
   curl localhost:8888/config-client/prod

   # クライアント経由で設定値を確認する
   curl localhost:8080/greeting
   # → こんにちは（デフォルト）
   ```

prod プロファイルでクライアントを起動し直すと、配信される設定が切り替わります
（クライアントのターミナルで Ctrl+C してから）。

```bash
../gradlew.bat bootRun --args='--spring.profiles.active=prod'
curl localhost:8080/greeting
# → こんにちは（本番）
```

## 設定変更 → refresh の手順

1. `src/main/resources/config-repo/config-client.yml` の `greeting.message` を変更して保存する
2. Config Server を「停止」→「実行」で再起動する（native プロファイルはクラスパスから配信するため）
3. `curl localhost:8080/greeting` → まだ古い値が返ることを確認する
4. `curl -X POST localhost:8080/actuator/refresh -H "Content-Type: application/json"`
   → 変更されたプロパティ名（`["greeting.message"]`）が返る
5. `curl localhost:8080/greeting` → クライアントを再起動していないのに新しい値が返ることを確認する

同じ手順は `client/curlメモ.txt` にもまとめてあります。

## 注意

- Config Server が止まっていてもクライアントは `optional:` により起動を試みますが、
  `greeting.message` には既定値がないため、他に値がなければ起動に失敗します
- `/actuator/refresh` が更新するのは、その要求を受け取ったクライアント 1 つだけです
- AWS Secrets Manager や Parameter Store との連携、Git バックエンドは、このサンプルの実装範囲に含まれません
- 配布コードは Spring Boot 4.1.0 と Spring Cloud BOM 2025.1.2 を指定しています（講座本編の説明どおり）
