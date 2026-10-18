# Codinable — 開発時の前提

Udemy 講座の受講環境（Electron 製のデスクトップ開発環境）。
受講者に配るのは**全講座入りの NSIS インストーラ 1 本だけ**で、Java / Node.js / Python /
Bash / HSQLDB と Java の言語サーバーを同梱している。講座ごとのインストーラは作らない
（数が増えると受講者が迷う）。更新はアプリ本体と講座で別々に行う（下の「更新」）。

利用者向けの説明・セットアップ手順・構成は @README.md にある。ここには
**コードを触るときに知っておく必要があること**だけを書く。

## 設計の骨格

- **プロジェクト = ワークスペース直下のフォルダ 1 つ**。既定は
  `%USERPROFILE%\codinable\<名前>`。一時ディレクトリへ書き出して動かすことはせず、
  そのフォルダをそのまま作業ディレクトリにして実行する
  （Gradle のビルドキャッシュも `node_modules` も 2 回目以降そのまま効く）
- **プロジェクトの種別は観測して決める**（`main/workspace.js` の `detectProject`）。
  `build.gradle` があれば gradle、`package.json` があれば node、といった判定で、
  実行できる対象（Gradle タスク / npm スクリプト）も実ファイルから取り出す。
  種別を設定ファイルに書かせない
- **講座固有のものは `courses/` にしか置かない**。アプリ本体は「雛形をコピーする仕組み」
  だけを持ち、どの講座が入っているかを知らない。講座追加でアプリ側を直す必要はない
- **コースは 3 か所から読む**（`main/courses.js` の `getCourseRoots`）。同梱（asar）→
  共有（`%PROGRAMDATA%\Codinable\courses`）→ 個人（`<userData>\courses`）の順で、
  同じ id は `version` の新しいほうを採る（同じなら後の置き場が勝つ）。ただし
  **開始した講座は開始したときの版に固定**する（config の `coursePins`。`loadCourses`）。
  **アプリを入れ直さずに講座だけ新しくできる**ことがこの構造の目的なので、
  同梱だけを前提にした読み込みへ戻さない。配信された版は個人の置き場に
  `<id>@<version>/` として入る。読み込み結果は `describeCourses` で
  設定画面に出しているため、置き場を増やすときはそちらも一緒に見る
- **実行は 1 本だけ**（`main/runner.js`）。新しく走らせるときは既存を止める。
  「今どれが走っているか」が常に 1 つに決まるようにしている。例外はシェルのスクリプト
  （`curl/01_get.sh` など）で、アプリが動いているあいだはアプリを止めずに脇で 1 本だけ動かす
  （`startSide`。API を叩く相手を落とさないため）。本体を止めると一緒に止まる
- **実行の口は 2 つ**（`renderer.js` の `runEntries` / `updateFileRunButton`）。
  ツールバーの「▶ アプリ実行」はアプリ全体（`workspace.js` の `pickRunEntries` が
  プロジェクトの中身から 1 つに決める。Gradle のタスクや npm のスクリプトを並べて
  選ばせることはしない）。テスト全体を流すボタンは置かない（ファイル単位の ▶ 実行と
  意味がかぶるため。テストはテストクラスを開いて流す）。
  エディタのタブの右端の「▶ 実行」は開いているファイルだけ（`fileRunTarget`。
  テストクラスは `test --tests`、main は `run -PmainClass` か init script の `codinableMain`、
  `.sql` / `.sh` / `.py` / 単体の `.js`）。単独で動かせるファイルはツリーに ▶ のバッジが付き
  （`listFileRuns` → `projectInfo.fileRuns`）、演習を選んだ直後は 3 秒ほど目立たせる
  （`flashExerciseFiles`）。Mentral Code IDE の「1 ファイル実行」と同じ考え方
- **Jakarta EE は `main/payara.js`**。`war` プラグインの雛形の ▶ アプリ実行は `gradle:payaraRun`
  （Gradle のタスクではない。runner.js が `gradle war` → `prepareRun` でドメインを作り直して
  Payara を起動時デプロイで起こす）。プレビューへ進むのはデプロイの完了をログで見てから
  （ポートは先に開くので waitForPort は使わない）。版は `src/payara-config.js`。
  呼び出し先の API や受信側のように WAR が 2 つ以上要る演習は、`codinable.services.json` の
  `"wars": ["<サブフォルダ>"]` で同じ Payara に一緒に載せる（付き添いを先、本体を最後にデプロイし、
  プレビューは本体の完了で開く）。データソースは雛形の `payara/post-boot-commands.asadmin` で
  インメモリの HSQLDB に登録し、テーブルと初期データは `persistence.xml` のスキーマ生成で
  デプロイのたびに作り直す（EclipseLink は 1 行 1 文しか流せないので、`build.gradle` の
  `flattenSql` が `sql/hsqldb` を整えて WAR に入れる。手本は `jakartaee-webapp-practical/templates/leaf-books`）
- **ルートの Gradle プロジェクトのソースは `src/main/java` か `src/backend/src/main/java`**
  （`workspace.js` の `GRADLE_JAVA_SOURCE`）。後者はバックエンドとフロントエンドを
  `src/backend`・`src/frontend` に分けたモノレポの配置で、講座原稿がその配置でパスを書いている雛形
  （Jakarta EE の leaf-books）だけが使う。テストクラスの ▶ 実行やツリーのバッジはこの 2 つを見る
- **Kafka / RabbitMQ は `main/messaging.js` で別管理**。必要時のみ起動し、
  通常の実行停止では落とさず、アプリ終了時に停止する。雛形ルートの
  `codinable.services.json` の `services` 配列で実行前の自動起動を宣言できる。
  データは userData/messaging に保存し、他のプロセスのポートを奪わない。
  バージョンは `src/messaging-config.js`、配布物の用意は `scripts/setup-messaging.ps1`。
  Erlang はユーザー領域に launcher と erl.ini を作り、同梱物に書き込まず起動する
- **サーバー（HSQLDB / Kafka / RabbitMQ / 付き添いのプロセス）は実行で自動起動し、
  起動・停止の口は出力欄の右の縦の列だけ**（`renderer.js` の `renderServiceControls`。
  サーバーごとに「● 名前」と起動・停止を縦に積む。タブの行には置かない）。
  そこに出るのは演習が使うもの（SQL の実行対象がある / `codinable.services.json`）と
  動いているものだけ。「実行」と意味がかぶるボタン（「SQL実行」など）や、
  タブの中の起動・停止ボタンは置かない
- **付き添いのプロセス**（書店から呼ばれる customer-hub など）は `main/companions.js`。
  `codinable.services.json` の `processes` に `{ id, dir, run, port, when }` で書く。
  「実行」のたびに起こし直し（DB が初期データに戻る）、実行の停止では止めない。
  別のプロジェクトを実行すると止める（同じポートを取り合わない）。出力は `[id]` を付けて
  実行結果に流す。道具の境界と同じく、起こすきっかけは「実行」とボタンだけにする。
  `test/check-companions.js` で押さえている
- **SQL の演習はファイルを流すたびに `reset.sql` で初期状態に戻す**（`runSqlFromEditor`）。
  雛形ルートの `reset.sql` を PUBLIC スキーマを消してから流し、そのあとで選んだファイルを流す。
  範囲選択だけを流すときは戻さない（別の演習の状態が残っているときだけ戻す）。
  `reset.sql` は実行対象に並べない。失敗したら何文目のどの文かを SQL タブに出す
- **LLM は任意機能**。API キーが無い状態でも他のすべてが動くことを壊さない
- **チャットは Ask と Agent の 2 つ**（`src/renderer/renderer.js` の `chatMode`）。
  Ask は読むだけ（`src/llm/index.js` の `streamChat`）。Agent は道具を使う
  （`src/main/agent.js` の `runAgent`）。どちらもプロジェクトのファイルと直近の実行結果は
  送信時に自動で渡す。**何を渡すかを受講者に選ばせない・外させない**（チップや ✕ は置かず、
  入力欄の上に 1 行の案内だけ。Mentral Code と同じ）
- **Agent の境界はプロンプトではなくコードで縛る**。`main/agent.js` の
  `resolveInExercise` が、いま開いている演習のディレクトリの外
  （`..` / ドライブ文字 / UNC / シンボリックリンク）と生成物のディレクトリを弾く。
  対象の演習は IPC で渡る `context.project` に固定で、道具の引数では変えられない。
  **道具を増やすときも「実行するもの」は足さない**。プロセスを起こすのは受講者の
  「実行」ボタンだけにしてあり、そうすることで何が動いているかが常に操作と一致する。
  この境界は `test/check-agent-bounds.js` と `test/check-agent-loop.js` で押さえている
- **モデルの書き換えは必ず差分として見せ、「元に戻す」を付ける**
  （`renderer.js` の `renderAgentEdit`）。書き込み自体は Agent が即座に行うが、
  黙って変わったように見せてはいけない
- **道具呼び出しは 3 社とも素の REST で扱う**（`src/llm/*.js` の `callWithTools`）。
  各社の Agent SDK は入れない。中立な形
  `{ text, toolCalls: [{ id, name, input }] }` に変換して返すのが各アダプタの仕事で、
  ループ側はプロバイダを知らない

## 触るときに気をつけること

- **表示言語は日本語と英語の 2 つだけ**。増減させるときは
  `src/app-config.js` の `PRODUCT.languages` /
  `src/renderer/i18n.js` / `src/renderer/index.html` の言語選択 /
  `assets/installer.nsh` / `builder/electron-builder.js` の `installerLanguages`
  を**そろえる**。インストーラでの選択は `$INSTDIR\default-lang.txt` 経由で
  初回起動時の表示言語になる（`src/main/config.js` の `readInstallerLang`）
- **`src/renderer/style.css` の「Codinable 追加・上書きスタイル」より上は
  Chatinable から持ってきた資産**。改善を取り込みやすくするため、差分は
  そのセクションに足す
- **エディタは esbuild でバンドルしている**。`src/renderer/editor/` を変えたら
  `npm run build:editor`（`npm start` は自動で走る）。出力
  `src/renderer/vendor/cm6.js` は生成物なので直接編集しない
- **ランタイムのパス解決は `src/main/runtimes.js` に集約**する。開発実行時は
  リポジトリ直下、パッケージ後は `process.resourcesPath` 配下を見るため、
  これを通さずに `path.join(__dirname, ...)` で組み立てると packaged で壊れる
- **`extraResources` に入れるもの / `asar` に入れるものの区別**。子プロセスとして
  実行するもの（`java.exe` / `node.exe` / `python.exe`）は実ファイルが必要なので
  `extraResources`。`courses/` は読むだけなので `asar` の中でよい
- **`electron-builder` の `extraMetadata` は使わない**。ソースの `package.json` を
  上書きし、`scripts` / `devDependencies` が消える
- **Gradle スクリプトは BOM なし UTF-8** で保存する（BOM 付きだと Gradle が起動しない）
- **Gemini の道具呼び出しは、受け取った parts を送り返す**（`thoughtSignature` が無いと 400）。
  アダプタは `replay` として返し、`agent.js` は中身を見ずに履歴へ積む。
  `test/check-gemini-replay.js` で押さえている
- **チャットの送信ボタンは、送信中そのまま「中断」になる**（`setChatStreaming`）。
  中断ボタンを別の場所に出さない。AI の応答とコードブロックにはコピーボタンを付ける

## 演習（`courses/*/course.yaml` の `exercises[]`）を追加・修正するとき

演習は「動かして確かめる 1 単位」で、講座のレッスンと 1 対 1 に対応させる。
サイドバー上段の一覧に出て、選ぶだけで作業用プロジェクトの用意・ファイルを開く・
実行ボタンの切り替え（= 実行環境の切り替え）まで済む。

- **`chapter` / `lesson` は実際の講座に合わせる**。`lesson` は一覧の副題に出るので、
  受講者が動画のどこと対応するか迷わない粒度（番号 + 名前）にする
- **`runtime`** は `java` / `spring` / `jakarta` / `node` / `react` / `python` / `static` /
  `sql` / `shell` のいずれか。アイコンとタグになる。増やすときは
  `renderer.js` の `RUNTIME_ICONS` と `i18n.js` の `runtime_*` を両方足す
- **`run` はどのボタンを主役にするか**（書式は `file:<パス>` / `sql:<パス>` /
  `gradle:<タスク>` / `npm:<スクリプト>` / `static:<ルート>`。Jakarta EE は `gradle:payaraRun`）。`gradle:test` なら
  ▶ アプリ実行は塗りつぶさず、テストクラスをツリーで光らせる（テストはテストクラスを
  開いて ▶ 実行で流す）。`gradle:build` のように ▶ アプリ実行と同じ種類のものは
  ▶ アプリ実行が動かすものを差し替える。`sql:` / `file:` はファイル単位で動かす演習の
  最初のファイル（ツリーで目立たせる。▶ アプリ実行は押せない）。
  当たるボタンがあるかは `workspace.js` が雛形の構成（`build.gradle` の有無、
  `package.json` の `scripts`、`index.html`）から決めるので、食い違わせない。
  `npx electron test/check-run-entries.js` で全講座を確かめる。説明文はボタンの名前
  （▶ アプリ実行 / エディタの右上の ▶ 実行）で案内する。
  API を叩く curl は `curl/NN_*.sh` に置き、ファイル単位で動かしてもらう。
  `sql:` のファイルは選ぶときには開かず、
  実行したときに開く（SQL の演習は最初に `README.md` のテーブル構成を読んでもらうため）
- **`preview` はプレビューで最初に開くパス**（`/hello` など）。実行でサーバーの URL を検知したとき、
  `/` に画面が無いアプリで 404 や 500 から始まらないように付ける（`renderer.js` の
  `withExercisePreviewPath`）。説明文で「〜を開いてください」と書いた場所をそのまま書く
- **`chapter` の名前は講座の `chapters:` に書く**（番号 → 日本語 / 英語）。演習一覧は
  チャプターごとのアコーディオンで、見出しは「チャプター7 JavaScriptとTypeScript」。
  名前は講座原稿の目次と同じにする。開いているチャプターは講座ごとに localStorage へ覚える
- **`openFiles`（演習を選んだときに開くファイル）はすべての演習で雛形直下の `README.md` 1 つ**。
  コードではなく、その演習の概要を最初に見せる。説明のファイルを分けない（`SCHEMA.md` は置かない）
  - 中身は タイトル / 何を学ぶ演習か / 見るファイル（主役のクラス・Controller・テストクラス・
    スクリプトを相対パスで）/ ボタンでの動かし方（▶ アプリ実行、「〇〇 を開いてエディタの右上の
    ▶ 実行」、curl などのスクリプト）/ 結果のどこを見るか。
  - DB・SQL を題材にする演習は、末尾に「この演習で使うテーブル」の節（テーブル構成・初期データ。
    SQL の演習は流す順番も）を `<!-- schema:begin -->` 〜 `<!-- schema:end -->` で囲んで置く
    20〜50 行程度に収め、コードに無い動きは書かない。同じ雛形を複数の演習で使うときは
    1 つの README.md でそれらをまとめて説明する
  - README.md には画面に無い操作（ターミナル、実行対象のセレクト、🧪 テスト実行）を書かない。
    テストは「〇〇Test.java を開いて ▶ 実行」、コマンドを打つ手順はスクリプト（`.sh`）にする
  - Markdown は最初からビューアで開く（「編集」で切り替え）
  - このルールは `node test/check-courses.js` で確かめる
- **DB の演習の README.md のテーブルの節は手で書かない**。`node scripts/build-schema-docs.js` が雛形の
  実際の DDL / DML を HSQLDB に流し、できたテーブル（型・主キー・外部キー・初期データ）と
  DDL の行末コメント（カラムの意味）から作り、`schema:begin` 〜 `schema:end` の間だけを
  書き換える（`scripts/SchemaDoc.java`）。DDL を直したら流し直す。DDL が無くエンティティから
  テーブルを作る演習だけは手書き（`MANUAL`）。SQL の演習（webapp-archi-overview の `sql-*`）の
  README.md は流す順番の説明を含むので全体を手書き
- **更新系の SQL で状態が変わる演習は、実行のたびに初期データへ戻す**。SQL の演習は
  `reset.sql`、Java / Spring の雛形はインメモリの HSQLDB に起動時に DDL / DML を流す
  （プロセスごとに別の DB）。「前に流した結果が残っていてエラー」を受講者に踏ませない
- **「↺ 初期化」は完全に戻す**（`workspace.js` の `resetToTemplate`）。自分で足した
  ファイルも消してから雛形を書き戻す。残すのは `.codinable`（演習との対応）と
  依存のキャッシュ（`node_modules` / `.gradle`）だけ
- **依存の用意は受講者に踏ませない**。`package.json` があって依存が
  揃っていなければ実行前に `npm install` が、`requirements.txt` があれば
  `pip install -r` が自動で走る（`runner.js` の `needsNpmInstall` /
  `pipInstallStep`）。雛形に「最初に install してください」と書く必要はない
- **`tabs` で出力欄のタブを演習ごとに出し分ける**（`output` / `tests` / `sql` /
  `messaging` / `preview`。`output` は常に出る。ターミナルのタブは無い）。書かなければ
  `courses.js` の `exerciseTabs` が runtime と雛形の中身（`build.gradle` → tests、
  `codinable.services.json` → messaging）から決めるので、ふつうは書かなくてよい。
  隠したタブでも、結果を出す必要があれば `showRunPane` が出す
- **問いを立てない**。`descriptions` は「何をどう動かすか」に徹し、
  正解・採点・完了といった語を持ち込まない

## 実行ボタンとプレビューの活性

- **ツールバーのボタンはプロジェクトの中身だけで決まる**（`pickRunEntries`）。
  エディタで選んでいるファイルには依存させない。README を開いた瞬間に
  アプリが動かせなくなる、という状態を作らないため。ファイル単位の「▶ 実行」だけが
  開いているファイルに従い、動かせないファイルではボタン自体を出さない
- **動かないものを実行の対象にしない**。`package.json` があるプロジェクトの `.js` は
  npm スクリプトが入口なので単体では動かさず、`index.html` の隣の `.js` は
  ブラウザで読まれる側なので動かさない。`static:` も npm スクリプトが
  あるときは使わない（Vite などは dev サーバー越しでないと動かない）。
  Gradle のプロジェクトの `.sql` はアプリが起動時に流すものなので単独では動かさない
- **プレビューは待ち受けているサーバーがあるときだけ活性**にする
  （`setPreviewAvailable`）。実行の出力から URL を検知したとき、または
  内蔵の静的サーバーを起こしたときに活性化し、実行が終わったら
  `refreshPreviewAvailability` で見直す。静的サーバーは実行プロセスとは
  別に生きるので、プロセスの終了だけを理由に落とさない
- **プレビューの口は出力欄の「🌐 Webブラウザ」タブだけ**（ツールバーにボタンは置かない）。
  `<webview>` へは `previewUrl` を通して読み込ませる。非表示のうちに `src` を
  差し替えてすぐ `reload` すると about:blank の ERR_ABORTED で打ち切られ、
  真っ白のままになる（先にタブを出し、用意ができていれば `loadURL`）

## コースを増やすとき

- **`course.yaml` に `version` を書く**。同じ id が複数の置き場にあるときの新旧判定に
  使う。書かないと `0` 扱いになり、他の置き場のものに必ず負ける
- **切り替えはヘッダー左の「新規コース開始・再開」ダイアログだけ**
  （`renderer.js` の `openCourseDialog` → `setActiveCourse`）。演習一覧は
  `currentCourse()` を通して読むので、コース選択の持ち主を増やさない
  （2 か所で選べると、どちらが正か分からなくなる）。タイトル下の帯は受講中の講座名の表示だけ
- **「再開」に出る状態は作業用プロジェクトそのもの**。別の保存先は持たず、
  ワークスペースにその講座のプロジェクトがあれば再開に並ぶ。最後に開いた演習だけ
  localStorage の `courseProgress` に控える。新規は最初の演習、再開は最後の演習を開く
- **`setActiveCourse` 自体は開いているプロジェクトを変えない**。プロジェクトを
  開き直すのはダイアログで講座を選んだとき（とその先の `openExercise`）だけ
- **`npm start` は常に全講座で起動する**（配布物が全講座入りの 1 本だけのため）。
  読み込む講座を絞る仕組みは持たない。E2E も全講座を読み込み、検査する講座を
  `setActiveCourse` で受講中にしてから流す
- 手で足して確かめるときは、設定画面の「📂 講座フォルダを開く」で個人フォルダを開き、
  置いたら「↻ 読み直す」を押す（`courses-reload` がキャッシュを捨てて読み直す）

## 雛形（`courses/*/templates/`）を追加・修正するとき

- **同梱ランタイムで動くことが条件**。Java は **Java 25 / Spring Boot 4**、
  Node.js は 24、Python は 3.13。講座本編が Maven や PostgreSQL を使っていても、
  雛形は Gradle と HSQLDB に寄せる
- Spring Boot 4 での改名に注意する。`spring-boot-starter-web` →
  `spring-boot-starter-webmvc`、`@WebMvcTest` は `spring-boot-webmvc-test` へ移動
- `gradlew` は同梱しなくてよい（無ければ `runner.js` が
  `resources/gradle-wrapper/` から補う）
- **雛形は単独で動く Gradle プロジェクトにする**。講座リポジトリの親 build.gradle や
  HSQLDB サーバー（9001）に頼らず、DB はインメモリ（`jdbc:hsqldb:mem:`）で起動時に SQL を流す
- **2 プロセス要る演習**（API とクライアント、Spring と React の画面など）は、主役を雛形直下に置き、
  もう片方はサブフォルダ（`api/` `consumer/` `frontend/` など）の独立プロジェクトにする。
  待ち受けるサーバー（呼び出し先の API・受信側・React の画面）は `codinable.services.json` の
  `processes` に書き、「▶ アプリ実行」で一緒に起動する。
  受講者が見る画面を持つものには `preview` を付け、動き出したらプレビューがそこへ移るようにする。
  起動して処理を流したら終わるクライアント（`client/` の Java コンソール）は、
  `client/run.sh`（ルートの `gradlew.bat` で `client/` の `run` を流す）を置き、受講者が開いて
  エディタの右上の ▶ 実行で動かす（アプリを動かしたまま脇で走る）。サブフォルダの `package.json` 配下は実行対象に出さない
  （`workspace.js` の `inNestedPackage`）
- **受講者はコマンドを打てない**（ターミナルは無い）。curl で API を叩く・Gatling を流す・
  ファイルを用意するといった手順は、雛形にシェルスクリプト（UTF-8 BOM なし・LF・
  `#!/usr/bin/env bash`・先頭に日本語で「何をするか / 前提（先に ▶ アプリ実行）」・コマンドの前に `set -x`）
  を置き、▶ 実行で動かしてもらう。同梱の bash で、作業ディレクトリはプロジェクト直下・
  curl / java / node が PATH に入った状態で動く
- Mockito を使う雛形は `mockito-core` を `-javaagent` で渡す（jlink 版 JDK に
  `jdk.attach` が入る前に作った runtime でもモックを作れるようにするため）
- 雛形を増やしたら `course.yaml` の `exercises[]` に追記し、雛形に演習の概要の `README.md` を
  書いて `openFiles` に指定する（上の「`openFiles`」のルール）。チャプターが増えたら
  `chapters:` に名前も足す。次のコマンドで確かめられる

```bash
cd desktop && node test/check-courses.js
```

## よく使うコマンド

```bash
# 準備（初回。同梱ランタイムを取得する）
./setup.bat
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only python -Force  # 一部だけ

cd desktop
npm start            # エディタバンドルをビルドして全講座で起動
npm run build:editor # エディタバンドルだけ作り直す
npm run pack         # 署名なしの win-unpacked（動作確認用）
npm run build        # インストーラ (.exe) と latest.yml
npm run build:courses # 講座の配信物（dist-updates/courses/）
npm run release:app      # インストーラを GitHub Releases へ（タグ v<version>、「最新」にする）
npm run release:courses  # 講座を GitHub Releases の courses タグへ上書き
```

## 更新（アプリ本体と講座は別々）

- **アプリ本体**は electron-updater（generic フィード）で更新する（`main/updater.js`。
  SP-AI Desktop Agent の `src/updater.js` と同じ設計）。起動の 5 秒後に黙って確認し、
  新しい版があるときだけ「今すぐ更新 / あとで」を聞く。設定画面の「更新を確認」は
  最新なら「最新です」まで出す。ダイアログは OS 標準ではなく画面の `#update-modal` で、
  main は表示の依頼とボタンの受け取りだけを行う。差分ダウンロードは使わず、
  120 秒進まなければ止める。**勝手にダウンロード・インストールしない**
- **講座**は「新しく始めるとき」だけ配信先を確認する（`courses.js` の
  `prepareCourseStart`）。新しい版があれば取り込んでから始め、無い・届かないときは
  手元の版で始める。どちらでもその版に固定する。**取り組み中の講座
  （作業用プロジェクトがある講座）は確認しない・変えない**。アプリの更新で同梱の版が
  上がっても変わらないよう、パッケージ後は固定するときに個人の置き場へ写しを取る
- 配信先は**このリポジトリ（公開）の GitHub Releases**（`src/app-config.js` の
  `UPDATES` / `getUpdateUrls`）。アプリ本体はタグ `v<version>` のリリースを「最新」にし、
  `releases/latest/download/latest.yml` を generic フィードとして読む。講座はタグ
  `courses` のリリース 1 つに上書きで置き、`releases/download/courses/index.json` を読む。
  **courses のリリースは「最新」にしない**（最新になるとアプリ本体の更新が見つからなくなる。
  `scripts/release.js` が `--latest=false` で作る）。GitHub のダウンロードは 302 で
  別ホストへ飛ぶので、取得は必ずリダイレクトを追う（テストの配信先も 302 を返す）
- 検証では環境変数 `CODINABLE_UPDATE_URL` で `<url>/app` と `<url>/courses` を見るように
  差し替えられる。`UPDATES.github` を空にすると更新を確認しない
- **version は semver にする**（`202609.1.0`）。`202609.01.00` のような 0 埋めは
  electron-updater が読めず更新確認が落ちる。画面では `formatVersion` で 0 埋めして見せる
- 講座を直して配るときは `course.yaml` の `version` を上げる。上げないと配信されない

## 変更したら確かめること

コードを変えたら、少なくとも次を通す。ここが壊れると受講者は講座を進められない。

1. `npm start` でウィンドウが出て、エラーがコンソールに出ないこと
2. 新規プロジェクトを雛形から作れること（`courses/` の読み込み）
3. Gradle の雛形で `bootRun` と `test` が通り、プレビューとテスト結果が出ること
4. 設定画面の同梱ランタイムに Java / Node.js / Python のバージョンが出ること
5. 講座の設計ルール（openFiles・チャプター名）: `node test/check-courses.js`
   演習の run と実行ボタンの対応・説明文のボタン名: `npx electron test/check-run-entries.js`
   講座の単独更新とタブの出し分け: `node test/check-course-update.js`
   （手元に配信先を立てて確かめる）
6. アプリ本体の更新（更新まわりを触ったとき）: `npm run pack` のあと
   `node test/check-app-update.js`（パッケージ後のアプリでだけ動く）
7. 講座の E2E（雛形・`course.yaml`・実行まわりを触ったとき）:
   `node test/e2e-courses.js <講座ID>[,…] [--only=<演習ID>,…]`。画面を操作して演習を 1 つずつ
   選び、主役のボタン・ファイル単位の ▶ 実行どおりに動くか（Web は HTTP の応答、test は成功、SQL は全ファイル、
   スクリプトは全ファイル）を見る。bootRun は 8080 を取り合うので、講座は 1 つずつ順に流す
   （`java-db-access` は 8080 を使わないので並べてよい）。Docker や外部サービスの登録が要る演習は
   置かない（受講者に用意の説明が要るため。Keycloak・Jaeger の演習は外した）。curl のスクリプトは
   応答のステータスを `HTTP/1.1 200` の形で出す（E2E がそれを見る。パイプに渡すときは
   `-w '%{stderr}HTTP/%{http_version} %{http_code}
'`）。わざと 5xx を見せるものは `CURL_EXPECTED_5XX` に足す
