# Codinable

Udemy 講座の受講環境として使う、Windows 向けのデスクトップ開発環境（Electron 製）。

エディタ・ファイルツリー・ターミナル・実行・テスト結果・Web プレビュー・SQL 実行を
1 つのウィンドウにまとめてあり、**Java / Node.js / Python / Bash / HSQLDB / Kafka / RabbitMQ を同梱**しているため、
受講者は個別に開発環境を作らなくても講座を始められる。

LLM とのチャットも使えるが、これは任意機能である（API キーを登録しなければ使わないだけで、
他の機能はすべて動く）。

## できること

| 機能 | 内容 |
|------|------|
| 演習 | 講座のレッスンに対応する実行単位。サイドバー上段の一覧から選ぶと、ファイルの用意から実行環境の選択までが済む |
| エディタ | CodeMirror 6。Java は言語サーバー（Eclipse JDT LS）による補完・定義ジャンプ・診断つき |
| 実行 | 何を動かすかは実行対象のセレクトで選ぶ。中身はプロジェクトから割り出されるので、どのファイルを開いていても変わらない。依存が足りなければ実行前に `npm install` / `pip install -r requirements.txt` を自動で通す |
| Spring Boot | `bootRun` でアプリを起動し、出力の URL を検知してプレビュータブを開く |
| テスト | `test` 実行後、JUnit の結果をメソッド単位で表示。JaCoCo のカバレッジをエディタに色で反映 |
| Web プレビュー | 開発サーバー（Spring Boot / Django / Express / Vite）と静的 HTML の両方に対応。**待ち受けているサーバーが見つかるまでプレビューは押せない**（押しても何も出ない状態を作らないため） |
| ターミナル | node-pty による本物の PTY。同梱ランタイムが PATH に入った状態で開く |
| SQL | 実行対象で選んだ `.sql` を HSQLDB（インメモリ）へ流し、結果を表で表示。DB が止まっていれば自動で起動する。開いているファイルの一部を選択していればその文だけ実行できる |
| メッセージング | Kafka / RabbitMQ の起動・停止、接続先・ログの確認、保存データの初期化。RabbitMQ の管理画面も開ける。演習が必要とするサーバーは実行前に自動起動する |
| LLM チャット | Claude Haiku / GPT Luna / Gemini Flash から選択（BYOK）。開いているプロジェクトのファイルは送信時に自動で渡す |
| Ask / Agent | チャット下部のラジオボタンで切り替える。**Ask** は読むだけで、答えるだけ。**Agent** は開いている演習のファイルを自分で読んで書き換える |
| Agent ができる範囲 | **開いている演習のディレクトリの中の読み書きだけ**。演習の外（`..` や絶対パス）と生成物（`node_modules` / `build` など）は弾かれる。**プログラムを動かすことはできない**（実行するのは受講者の「実行」ボタンだけ） |
| Agent の書き換え | 変更は必ず差分で表示され、「元に戻す」で戻せる |

表示言語は**日本語と英語**の 2 つ。インストーラの 1 ページ目で選び、
後からアプリの設定画面でも切り替えられる。

## 準備

Windows + Git Bash で、リポジトリ直下の `setup.bat` を実行する（初回は 10 分以上かかる）。

```bash
./setup.bat
```

同梱ランタイム・サーバー本体はサイズが大きいため Git では追跡していない。
`setup.bat` が入れるものは次の通り。

| 置き場所 | 中身 |
|---|---|
| `runtime/java/` | Temurin JDK 25 を jlink で最小化したもの（`javac` 同梱） |
| `runtime/node/` | Node.js 24（`npm` / `npx` 同梱） |
| `runtime/python/` | Python 3.13（embeddable 版に `pip` を入れたもの） |
| `runtime/bash/` | PortableGit から抜いた `bash` + coreutils + `curl` |
| `runtime/kafka/` | Apache Kafka 4.3.1（KRaft、学習用の単一ノード） |
| `runtime/rabbitmq/` | RabbitMQ 4.3.6（管理プラグインを有効にして起動） |
| `runtime/erlang/` | Erlang/OTP 28.5 と起動に必要な Visual C++ ランタイム DLL |
| `hsqldb/` | HSQLDB の jar |
| `resources/jdtls/` | Java の言語サーバー（Eclipse JDT LS） |
| `resources/gradle-wrapper/` | Gradle Wrapper の jar（雛形に `gradlew` が無くても実行できるように） |

一部だけ入れ直したいときは対象を指定する。

```bash
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only python,node
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only java -Force
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only kafka,rabbitmq
```

## 起動とビルド

```bash
cd desktop
npm start   # エディタバンドルをビルドしてから起動（読み込むコースを聞かれる）
npm run pack   # dist-installer/win-unpacked (署名なし・動作確認用)
npm run build  # dist-installer/Codinable-setup-<version>.exe
```

`npm start` は起動前に `courses/` のコース一覧を出し、読み込むコースを選ばせる。
受講者の手元（講座ごとのインストーラで入れたコースが 1 つ以上並ぶ状態）を再現して確かめるためのもの。

```bash
npm start                                  # 一覧から番号で選ぶ。Enter だけなら全コース（統合版）
npm start -- all                           # 全コース（聞かずに起動）
npm start -- spring-advanced               # そのコースだけ
npm start -- spring-advanced,java-db-access  # 2 つ（一覧の番号でも指定できる: npm start -- 3,7）
```

絞り込みは同梱・共有・個人のすべての置き場に効き、設定画面の「インストールされている講座」に
「開発用の絞り込み」として出る。パッケージ後のアプリには効かない。
コースが 1 つだけのときヘッダーのコース選択は講座名の表示だけになり、
2 つ以上で選べるようになる（`node test/check-course-filter.js` で確かめられる）。

## 構成

```text
codinable/
├── desktop/            Electron アプリ本体
│   ├── src/main.js       メインプロセス。IPC の入口
│   ├── src/app-config.js 製品設定（言語・同梱ランタイム・LLM モデル一覧）
│   ├── src/main/         設定 / ランタイム解決 / 実行 / ターミナル / SQL / ワークスペース / コース
│   ├── src/llm/          BYOK の LLM クライアント（Anthropic / OpenAI / Google）
│   ├── src/main/agent.js Agent モード（道具は「一覧・読む・書く」の 3 つだけ）
│   ├── src/renderer/     画面（3 ペイン: プロジェクト / エディタ・出力 / チャット）
│   ├── builder/          esbuild によるエディタバンドル生成と electron-builder 設定
│   └── assets/           アイコン・インストーラ画像・NSIS カスタムスクリプト
├── courses/            コースパック（講座ごとの雛形）
├── runtime/ hsqldb/ resources/   同梱ランタイム（setup.bat が用意）
└── scripts/setup.ps1   セットアップ本体
```

受講者のプロジェクトは `%USERPROFILE%\codinable\<プロジェクト名>` に作られる
（設定画面で場所を変更できる）。ふつうのフォルダなので、同じものを VS Code や
IntelliJ で開いても成立する。

## 演習

### Kafka / RabbitMQ

講座 **「Spring 高度な仕組み」** のチャプター 7 の演習（`spring-kafka-employee` /
`spring-amqp-employee`）を開いて「実行」を押すと、必要なサーバーが起動して
Spring の送信側（producer）が動く。受信側（consumer）はターミナルから起動する。
初回の Gradle / ライブラリ取得にはインターネット接続が必要。
サーバー本体・Erlang はインストーラに含まれ、受講者による Docker や Windows サービスの導入は不要。

**「メッセージング」タブ**では、各サーバーの起動・停止、接続先とログの確認、
停止後のデータ初期化ができる。通常の「実行停止」は演習プログラムだけを停止する。
サーバーは演習の実行後も動き続け、アプリを終了すると停止する。

| 接続先 | 既定値 |
|---|---|
| Kafka | `127.0.0.1:9092` |
| RabbitMQ（AMQP） | `amqp://guest:guest@127.0.0.1:5672/%2F`（vhost `/`） |
| RabbitMQ 管理画面 | `http://127.0.0.1:15672`（ユーザー名・パスワードともに `guest`） |

待ち受けはローカル PC のみ。Kafka の内部通信に 9093、RabbitMQ の内部通信に 25672 と
43690 も使う。他のソフトが必要なポートを使用中ならエラーを表示し、そのプロセスは停止しない。

保存先は `%APPDATA%\Codinable\messaging\kafka` と `rabbitmq`。
データは再起動後も残り、同じ Codinable の演習間で共有する。
**「データを初期化」は選んだサーバーの全演習分のデータを削除する**ため、画面で確認してから実行する。
Kafka の通常トピックの保持上限は 24 時間 / パーティションあたり 128 MiB（削除は定期実行）。

新しい演習で自動起動を使う場合は、雛形のルートに `codinable.services.json` を置く。

```json
{ "services": ["kafka", "rabbitmq"] }
```

必要なものだけ指定する。演習とターミナルには `KAFKA_BOOTSTRAP_SERVERS`、`RABBITMQ_URL`、
Spring Boot 用の `SPRING_KAFKA_BOOTSTRAP_SERVERS` / `SPRING_RABBITMQ_*` を渡す。
送信側・受信側を別プログラムとして常駐させるときは、実行ボタンとターミナルを使う。

同梱物の取得だけを行う開発用コマンド（Java セットアップ済みの場合）:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/setup-messaging.ps1
```

バージョンの定義は `desktop/src/messaging-config.js`。公式配布物を固定バージョンで取得し、
Kafka は公式 SHA-512、GitHub 配布物は公開されている SHA-256 digest がある場合に照合する。
配布物の LICENSE / NOTICE を保持し、Erlang のライセンス文書も同梱する。

### 演習の操作

**演習（exercise）** は「動かして確かめる 1 単位」で、講座のレッスンと 1 対 1 に
対応する。問題を出して解いてもらうものではない。

サイドバー上段に、チャプターの区切りを挟んだフラットな一覧として並ぶ。
演習を選ぶと次が自動で行われるので、あとは「実行」を押すだけでよい。

1. 作業用プロジェクトを雛形から用意する（2 回目以降は前回の続きを開く）
2. その演習の主要なファイルをエディタで開く
3. 実行対象を選ぶ（= Java / React / Python / Node.js / SQL / 静的 Web の切り替え）

実行対象のセレクトには、そのプロジェクトで動かせるものがすべて並ぶ。
1 つの演習に複数のファイル（`01_…` `02_…` のような連番）がある場合は、
ここで選び替えて順に動かす。エディタで別のファイルを開いても選択は変わらない。

書き換えて動かなくなったら、プロジェクト名の隣にある **「↺ 初期化」** で
その演習のファイルを配布時の状態に戻せる。自分で増やしたファイルは残るので、
雛形のファイルだけが書き戻る。

## AI に手伝ってもらう（Ask / Agent）

チャットの下にあるラジオボタンで、AI のふるまいを 2 つから選ぶ。

| | できること | 使う場面 |
|---|---|---|
| **Ask** | 読むだけ。質問に答える | 「このコードは何をしている？」「このエラーの意味は？」 |
| **Agent** | 開いている演習のファイルを読んで書き換える | 「この画面にボタンを足して」「このエラーを直して」 |

どちらのモードでも、開いている演習のファイルは送信時に自動で渡される
（添付の操作は要らない）。

Agent には、はっきりした境界を設けてある。

- 触れるのは**いま開いている演習のディレクトリの中だけ**。他の演習や PC の
  他の場所は読めないし書けない（`node_modules` や `build` などの生成物も対象外）
- **プログラムを動かすことはできない**。動かして確かめるのは受講者の役割で、
  Agent は「どの実行対象を選んで実行を押せばよいか」を伝えるだけ
- 書き換えは**必ず差分で表示**され、**「元に戻す」**で戻せる。
  演習ごとまとめて戻したいときは「↺ 初期化」を使う

## 複数の講座を 1 つの Codinable で使う

講座（コース）は 1 つずつ足せる。2 つ目の講座を受けるときにアプリを入れ直す必要はなく、
**講座の分だけ演習が増える**。今どの講座を見るかは、画面左上の 🎓 で切り替える
（開いているプロジェクトはそのままなので、作業を続けながら別の講座を覗ける）。

コースの置き場は 3 つあり、次の順に読む。

| 置き場 | 場所 | 誰が置くか |
|---|---|---|
| アプリ同梱 | アプリの中（`courses/`） | そのインストーラが持ってきた講座 |
| 共有フォルダ | `%PROGRAMDATA%\Codinable\courses` | 講座単位のインストーラ |
| 個人フォルダ | `%APPDATA%\Codinable\courses` | 手で足す・差し替える |

同じ講座 ID が複数の置き場にあるときは、`course.yaml` の `version` が新しいほうが使われる
（同じなら後の置き場が勝つ）。**設定画面の「インストールされている講座」**に、
読み込まれた講座とその出どころ・置き場のパスが出るので、
「足したのに出ない」ときはそこを見る。足した直後は同じ画面の「↻ 読み直す」で反映できる
（再起動は不要）。

## コースパックの追加

上の置き場のどれかにディレクトリを 1 つ増やし、`course.yaml` と `templates/` を置くだけでよい。
アプリ側の変更は不要。

```text
courses/<講座ID>/
├── course.yaml          講座名・説明・version・演習の一覧（日本語と英語）
└── templates/<演習ID>/  そのまま作業用ディレクトリにコピーされる中身
```

`course.yaml` の書き方は `courses/webapp-archi-overview/course.yaml` の
コメントを参照。収録済みの講座・サンプルは次のとおり。

| 講座ID | 対応する Udemy 講座 | 演習 |
|---|---|---|
| `webapp-archi-overview` | Web システム＋生成AI 技術概要編 | 全 30 件（下記） |
| `spring-boot-webapp-basic` | Spring Boot Webアプリ基礎 | 24 件（DI / MVC・Thymeleaf / 入力検証 / PRG・セッション / ファイル / JDBC・JPA・MyBatis / トランザクション / REST / RestClient） |
| `spring-advanced` | Spring 高度な仕組み | 13 件（WebClient・WebSocket / OAuth2・OIDC / Security / GraphQL / WebFlux・Kafka・RabbitMQ / Resilience・Retry / Actuator・Observability） |
| `spring-boot-cicd` | Spring Boot CICD | 9 件（JUnit・Mockito / Spring の結合テスト / Database Rider・API テスト / Playwright / Gatling / CI） |
| `spring-cloud-aws` | Springクラウド機能 on AWS | 6 件（ECS / Spring Session・Cloud Config / Cognito） |
| `spring-boot-webapp-practical` | Spring Boot Webアプリ応用（ECサイト） | 8 件（書店アプリ MVC 版・REST+React 版と、外部 API 役の customer-hub） |
| `java-db-access` | Java DBアクセス | 35 件（JDBC / MyBatis / JPA のロック・関連・JPQL・Criteria・複合主キー・継承） |

講座のサンプルリポジトリ（`learn_spring_aidd_*` / `learn_java_db_aidd`）は、ルートの
共通 build.gradle と HSQLDB サーバー（9001）を前提にしたマルチプロジェクトになっている。
雛形はそれを**演習ごとに単独で動く Gradle プロジェクト**に畳み、DB はインメモリ
（一部はファイル）の HSQLDB に切り替えて起動時に SQL を流す形にしてある。
2 つのプロセスが要る演習（API とクライアント、Spring と React の画面、producer と consumer）は、
主役を雛形直下に置いて「実行」で動かし、もう片方はサブフォルダからターミナルで起動する
（手順は各雛形の README.md）。

`webapp-archi-overview` の演習は次のとおり。チャプター 3 と 6 は、
Web ブラウザと DB の仕組みをその場で動かして確かめられるよう細かく分けてある。

| チャプター | 演習 |
|---|---|
| 3 HTML | 基本構造 / リストとテーブル / セマンティック要素 / フォーム |
| 3 JavaScript | DOM 操作 / イベント / フォーム処理 / fetch |
| 3 CSS | セレクタと色 / ボックスモデル / 配置と重なり / Flexbox・Grid / レスポンシブ |
| 3 まとめ | HTML・CSS・JavaScript を組み合わせた名簿管理ページ |
| 4 HTTP | curl でリクエストを送る |
| 6 SQL | 検索（SELECT・WHERE）/ CRUD / 集計と整形 / DDL と制約 / 結合・サブクエリ・ビュー |
| 7 言語 | JavaScript・TypeScript |
| 8 サーバーサイド | Spring MVC / Django / Express |
| 9 REST API | Spring REST + JUnit |
| 10 SPA | JSX・コンポーネント / State・Props / フォーム・イベント / Fetch・カスタムフック / React総合 |

## 注意事項

- Windows 前提（同梱ランタイムと NSIS インストーラが Windows 向け）
- Java の雛形は **Java 25 / Spring Boot 4** に合わせてある。講座本編が別の構成
  （Maven や PostgreSQL）を使っている場合は、雛形側を同梱ランタイムで動く形に寄せている
- アプリは既定で 8080 を使う雛形が複数あるため、同時に起動できない
- `runtime/` `hsqldb/` `resources/jdtls/` `resources/gradle-wrapper/`
  `desktop/src/renderer/vendor/` は生成物。直接編集しない
