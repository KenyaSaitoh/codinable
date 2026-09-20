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
- **実行は 1 本だけ**（`main/runner.js`）。新しく走らせるときは既存を止める。
  「今どれが走っているか」が常に 1 つに決まるようにしている
- **LLM は任意機能**。API キーが無い状態でも他のすべてが動くことを壊さない

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

## 雛形（`courses/*/templates/`）を追加・修正するとき

- **同梱ランタイムで動くことが条件**。Java は **Java 25 / Spring Boot 4**、
  Node.js は 24、Python は 3.13。講座本編が Maven や PostgreSQL を使っていても、
  雛形は Gradle と HSQLDB に寄せる
- Spring Boot 4 での改名に注意する。`spring-boot-starter-web` →
  `spring-boot-starter-webmvc`、`@WebMvcTest` は `spring-boot-webmvc-test` へ移動
- `gradlew` は同梱しなくてよい（無ければ `runner.js` が
  `resources/gradle-wrapper/` から補う）
- 雛形を増やしたら `course.yaml` の `templates[]` に追記し、`openFiles` に
  実在するパスを書く。次のコマンドで両方を確かめられる

```bash
cd desktop && node -e "
const yaml=require('js-yaml'),fs=require('fs'),path=require('path');
const dir='../courses/<講座ID>';
for (const t of yaml.load(fs.readFileSync(dir+'/course.yaml','utf8')).templates) {
  const d=path.join(dir,'templates',t.dir||t.id);
  const miss=(t.openFiles||[]).filter(f=>!fs.existsSync(path.join(d,f)));
  console.log((fs.existsSync(d)?'OK ':'NG ')+(t.dir||t.id), miss.join(', '));
}"
```

## よく使うコマンド

```bash
# 準備（初回。同梱ランタイムを取得する）
./setup.bat
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only python -Force  # 一部だけ

cd desktop
npm start            # エディタバンドルをビルドして起動
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
