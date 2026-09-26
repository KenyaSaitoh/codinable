# Codinable — 開発時の前提

Udemy 講座の受講環境（Electron 製のデスクトップ開発環境）。
受講者に配るのは NSIS インストーラ 1 本で、Java / Node.js / Python / Bash / HSQLDB と
Java の言語サーバーを同梱している。

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
  同じ id は `version` の新しいほうを採る（同じなら後の置き場が勝つ）。
  **アプリを入れ直さずに講座だけ増える**ことがこの構造の目的なので、
  同梱だけを前提にした読み込みへ戻さない。読み込み結果は `describeCourses` で
  設定画面に出しているため、置き場を増やすときはそちらも一緒に見る
- **実行は 1 本だけ**（`main/runner.js`）。新しく走らせるときは既存を止める。
  「今どれが走っているか」が常に 1 つに決まるようにしている
- **Kafka / RabbitMQ は `main/messaging.js` で別管理**。必要時のみ起動し、
  通常の実行停止では落とさず、アプリ終了時に停止する。雛形ルートの
  `codinable.services.json` の `services` 配列で実行前の自動起動を宣言できる。
  データは userData/messaging に保存し、他のプロセスのポートを奪わない。
  バージョンは `src/messaging-config.js`、配布物の用意は `scripts/setup-messaging.ps1`。
  Erlang はユーザー領域に launcher と erl.ini を作り、同梱物に書き込まず起動する
- **LLM は任意機能**。API キーが無い状態でも他のすべてが動くことを壊さない
- **チャットは Ask と Agent の 2 つ**（`src/renderer/renderer.js` の `chatMode`）。
  Ask は読むだけ（`src/llm/index.js` の `streamChat`）。Agent は道具を使う
  （`src/main/agent.js` の `runAgent`）。どちらもプロジェクトのファイルは
  送信時に自動で渡す
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

## 演習（`courses/*/course.yaml` の `exercises[]`）を追加・修正するとき

演習は「動かして確かめる 1 単位」で、講座のレッスンと 1 対 1 に対応させる。
サイドバー上段の一覧に出て、選ぶだけで作業用プロジェクトの用意・ファイルを開く・
実行対象の選択（= 実行環境の切り替え）まで済む。

- **`chapter` / `lesson` は実際の講座に合わせる**。`lesson` は一覧の副題に出るので、
  受講者が動画のどこと対応するか迷わない粒度（番号 + 名前）にする
- **`runtime`** は `java` / `spring` / `node` / `react` / `python` / `static` /
  `sql` / `shell` のいずれか。アイコンとタグになる。増やすときは
  `renderer.js` の `RUNTIME_ICONS` と `i18n.js` の `runtime_*` を両方足す
- **`run` は実行対象セレクトと同じ書式**（`file:<パス>` / `sql:<パス>` /
  `gradle:<タスク>` / `npm:<スクリプト>` / `static:<ルート>` / `java`）。
  ここで指定した選択肢が実際に出るかは `workspace.js` の `detectProject` が
  決めるので、雛形の構成（`build.gradle` の有無、`package.json` の `scripts`、
  `index.html`）と食い違わせない。選択肢が無いときは黙って無視される。
  `file:` と `sql:` はパスも指定するので、そのファイルを先に開いてから選ぶ
- **依存の用意は受講者に踏ませない**。`package.json` があって依存が
  揃っていなければ実行前に `npm install` が、`requirements.txt` があれば
  `pip install -r` が自動で走る（`runner.js` の `needsNpmInstall` /
  `pipInstallStep`）。雛形に「最初に install してください」と書く必要はない
- **問いを立てない**。`descriptions` は「何をどう動かすか」に徹し、
  正解・採点・完了といった語を持ち込まない

## 実行対象とプレビューの活性

- **実行対象はプロジェクトの中身だけで決まる**（`detectProject` の
  `runnableFiles`）。エディタで選んでいるファイルには依存させない。
  README を開いた瞬間に実行できなくなる、という状態を作らないため
- **動かない選択肢を並べない**。`package.json` があるプロジェクトの `.js` は
  npm スクリプトが入口なので単体では並べず、`index.html` の隣の `.js` は
  ブラウザで読まれる側なので並べない。`static:` も npm スクリプトが
  あるときは出さない（Vite などは dev サーバー越しでないと動かない）
- **プレビューは待ち受けているサーバーがあるときだけ活性**にする
  （`setPreviewAvailable`）。実行の出力から URL を検知したとき、または
  内蔵の静的サーバーを起こしたときに活性化し、実行が終わったら
  `refreshPreviewAvailability` で見直す。静的サーバーは実行プロセスとは
  別に生きるので、プロセスの終了だけを理由に落とさない

## コースを増やすとき

- **`course.yaml` に `version` を書く**。同じ id が複数の置き場にあるときの新旧判定に
  使う。書かないと `0` 扱いになり、他の置き場のものに必ず負ける
- **切り替えはヘッダー左の `active-course-select` だけ**。演習一覧は
  `currentCourse()` を通して読むので、コース選択の持ち主を増やさない
  （2 か所で選べると、どちらが正か分からなくなる）
- **コースを切り替えても開いているプロジェクトは変えない**。別の講座を見ながら
  今の作業を続けられるようにしてある
- **開発時は `npm start` で読み込むコースを絞れる**（`scripts/start.js` →
  環境変数 `CODINABLE_COURSES` → `courses.js` の `getDevCourseFilter`）。
  全置き場に効き、パッケージ後は無視する。1 コース / 複数コースの画面の違いは
  `test/check-course-filter.js` で押さえている
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
  もう片方はサブフォルダ（`api/` `client/` `frontend/` など）の独立プロジェクトにして
  ターミナルから起動する。サブフォルダの `package.json` 配下は実行対象に出さない
  （`workspace.js` の `inNestedPackage`）
- Mockito を使う雛形は `mockito-core` を `-javaagent` で渡す（jlink 版 JDK に
  `jdk.attach` が入る前に作った runtime でもモックを作れるようにするため）
- 雛形を増やしたら `course.yaml` の `exercises[]` に追記し、`openFiles` に
  実在するパスを書く。次のコマンドで両方を確かめられる

```bash
cd desktop && node -e "
const yaml=require('js-yaml'),fs=require('fs'),path=require('path');
const dir='../courses/<講座ID>';
for (const e of yaml.load(fs.readFileSync(dir+'/course.yaml','utf8')).exercises) {
  const d=path.join(dir,'templates',e.dir||e.id);
  const miss=(e.openFiles||[]).filter(f=>!fs.existsSync(path.join(d,f)));
  console.log((fs.existsSync(d)?'OK ':'NG ')+(e.dir||e.id), miss.join(', '));
}"
```

## よく使うコマンド

```bash
# 準備（初回。同梱ランタイムを取得する）
./setup.bat
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only python -Force  # 一部だけ

cd desktop
npm start            # エディタバンドルをビルドして起動（読み込むコースを選ぶ。Enter で全コース）
npm start -- all     # 全コース（統合版）で起動 / `-- <id>,<id>` で絞る（scripts/start.js）
npm run build:editor # エディタバンドルだけ作り直す
npm run pack         # 署名なしの win-unpacked（動作確認用）
npm run build        # インストーラ (.exe)
```

## 変更したら確かめること

コードを変えたら、少なくとも次を通す。ここが壊れると受講者は講座を進められない。

1. `npm start` でウィンドウが出て、エラーがコンソールに出ないこと
2. 新規プロジェクトを雛形から作れること（`courses/` の読み込み）
3. Gradle の雛形で `bootRun` と `test` が通り、プレビューとテスト結果が出ること
4. ターミナルで `java -version` / `node -v` / `python --version` が答えること
