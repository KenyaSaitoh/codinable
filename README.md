# Codinable

Udemy 講座の受講環境として使う、Windows 向けのデスクトップ開発環境（Electron 製）。

エディタ・ファイルツリー・ターミナル・実行・テスト結果・Web プレビュー・SQL 実行を
1 つのウィンドウにまとめてあり、**Java / Node.js / Python / Bash / HSQLDB を同梱**しているため、
受講者は個別に開発環境を作らなくても講座を始められる。

LLM とのチャットも使えるが、これは任意機能である（API キーを登録しなければ使わないだけで、
他の機能はすべて動く）。

## できること

| 機能 | 内容 |
|------|------|
| 演習 | 講座のレッスンに対応する実行単位。サイドバー上段の一覧から選ぶと、ファイルの用意から実行環境の選択までが済む |
| エディタ | CodeMirror 6。Java は言語サーバー（Eclipse JDT LS）による補完・定義ジャンプ・診断つき |
| 実行 | Gradle タスク / npm スクリプト / 単体の Java・Python・Node.js・Bash ファイル。依存が足りなければ実行前に `npm install` / `pip install -r requirements.txt` を自動で通す |
| Spring Boot | `bootRun` でアプリを起動し、出力の URL を検知してプレビュータブを開く |
| テスト | `test` 実行後、JUnit の結果をメソッド単位で表示。JaCoCo のカバレッジをエディタに色で反映 |
| Web プレビュー | 開発サーバー（Spring Boot / Flask / Express / Vite）と静的 HTML の両方に対応 |
| ターミナル | node-pty による本物の PTY。同梱ランタイムが PATH に入った状態で開く |
| SQL | `.sql` を開いて「実行」を押すと HSQLDB（インメモリ）へ流し、結果を表で表示。DB が止まっていれば自動で起動する。一部を選択すればその文だけ実行できる |
| LLM チャット | Claude Haiku / GPT Luna / Gemini Flash から選択（BYOK） |

表示言語は**日本語と英語**の 2 つ。インストーラの 1 ページ目で選び、
後からアプリの設定画面でも切り替えられる。

## 準備

Windows + Git Bash で、リポジトリ直下の `setup.bat` を実行する（初回は 10 分以上かかる）。

```bash
./setup.bat
```

同梱ランタイム（合計 300MB 超）を取得するため Git では追跡していない。
`setup.bat` が入れるものは次の通り。

| 置き場所 | 中身 |
|---|---|
| `runtime/java/` | Temurin JDK 25 を jlink で最小化したもの（`javac` 同梱） |
| `runtime/node/` | Node.js 24（`npm` / `npx` 同梱） |
| `runtime/python/` | Python 3.13（embeddable 版に `pip` を入れたもの） |
| `runtime/bash/` | PortableGit から抜いた `bash` + coreutils + `curl` |
| `hsqldb/` | HSQLDB の jar |
| `resources/jdtls/` | Java の言語サーバー（Eclipse JDT LS） |
| `resources/gradle-wrapper/` | Gradle Wrapper の jar（雛形に `gradlew` が無くても実行できるように） |

一部だけ入れ直したいときは対象を指定する。

```bash
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only python,node
powershell -ExecutionPolicy Bypass -File scripts/setup.ps1 -Only java -Force
```

## 起動とビルド

```bash
cd desktop
npm start   # エディタバンドルをビルドしてから起動
npm run pack   # dist-installer/win-unpacked (署名なし・動作確認用)
npm run build  # dist-installer/Codinable-setup-<version>.exe
```

## 構成

```text
codinable/
├── desktop/            Electron アプリ本体
│   ├── src/main.js       メインプロセス。IPC の入口
│   ├── src/app-config.js 製品設定（言語・同梱ランタイム・LLM モデル一覧）
│   ├── src/main/         設定 / ランタイム解決 / 実行 / ターミナル / SQL / ワークスペース / コース
│   ├── src/llm/          BYOK の LLM クライアント（Anthropic / OpenAI / Google）
│   ├── src/renderer/     画面（3 ペイン: ファイル / エディタ・出力 / チャット）
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

**演習（exercise）** は「動かして確かめる 1 単位」で、講座のレッスンと 1 対 1 に
対応する。問題を出して解いてもらうものではない。

サイドバー上段に、チャプターの区切りを挟んだフラットな一覧として並ぶ。
演習を選ぶと次が自動で行われるので、あとは「実行」を押すだけでよい。

1. 作業用プロジェクトを雛形から用意する（2 回目以降は前回の続きを開く）
2. その演習の主要なファイルをエディタで開く
3. 実行対象を選ぶ（= Java / React / Python / Node.js / 静的 Web の切り替え）

## コースパックの追加

`courses/` にディレクトリを 1 つ増やし、`course.yaml` と `templates/` を置くだけでよい。
アプリ側の変更は不要で、起動時に読み込まれて演習一覧に並ぶ。

```text
courses/<講座ID>/
├── course.yaml          講座名・説明・演習の一覧（日本語と英語）
└── templates/<演習ID>/  そのまま作業用ディレクトリにコピーされる中身
```

`course.yaml` の書き方は `courses/webapp-archi-overview/course.yaml` の
コメントを参照。収録済みの講座は次の 1 つ。

| 講座ID | 対応する Udemy 講座 | 演習 |
|---|---|---|
| `webapp-archi-overview` | Web システム＋生成AI 技術概要編 | 静的 Web ページ / HTTP（curl）/ SQL / JavaScript・TypeScript / Spring MVC / Flask / Express / REST API + JUnit / React SPA |

## 注意事項

- Windows 前提（同梱ランタイムと NSIS インストーラが Windows 向け）
- Java の雛形は **Java 25 / Spring Boot 4** に合わせてある。講座本編が別の構成
  （Maven や PostgreSQL）を使っている場合は、雛形側を同梱ランタイムで動く形に寄せている
- アプリは既定で 8080 を使う雛形が複数あるため、同時に起動できない
- `runtime/` `hsqldb/` `resources/jdtls/` `resources/gradle-wrapper/`
  `desktop/src/renderer/vendor/` は生成物。直接編集しない
