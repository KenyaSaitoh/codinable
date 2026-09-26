---
name: codinable-start
description: Codinable を開発用に起動する（npm start）。読み込むコースを選んで起動する。「統合版で起動して」「全コースで起動」「Spring 高度な仕組みだけで起動」「基礎と DB アクセスの 2 コースで立ち上げて」「コース 1 つの状態で確認したい」「Codinable を動かして」のような日本語の指示で使う。受講者の手元（講座ごとのインストーラで入れたコースが 1 つ以上並ぶ状態）を再現してテストするためのもの。
---

# Codinable を読み込むコースを選んで起動する

`desktop/` の `npm start` は `desktop/scripts/start.js` を通して Electron を起動する。
引数で読み込むコースを指定すると、環境変数 `CODINABLE_COURSES` として渡され、
`desktop/src/main/courses.js` の `getDevCourseFilter` が同梱・共有・個人の全置き場を絞り込む。
パッケージ後のアプリには効かない（開発時専用）。

## 手順

1. **コースの一覧を実物から読む**（コースは増減するので、下の表を当てにしすぎない）。

   ```bash
   cd desktop && node -e "
   const yaml=require('js-yaml'),fs=require('fs'),p=require('path');const d='../courses';
   fs.readdirSync(d).filter(n=>fs.existsSync(p.join(d,n,'course.yaml'))).map(n=>yaml.load(fs.readFileSync(p.join(d,n,'course.yaml'),'utf8')))
     .sort((a,b)=>(a.order??999)-(b.order??999)).forEach((m,i)=>console.log(i+1,m.id,m.names.ja,'演習'+(m.exercises||[]).length))"
   ```

2. **指示をコース id に対応付ける**。講座名の一部・略称・Udemy の番号で言われることが多い。

   | 言われ方の例 | 指定 |
   |---|---|
   | 統合版 / 全コース / 全部入り / 何も言わない | `all` |
   | 概要編 / Web システム＋生成AI | `webapp-archi-overview` |
   | 基礎 / Spring Boot Web アプリ基礎 / 02 | `spring-boot-webapp-basic` |
   | 高度 / 発展 / 03 | `spring-advanced` |
   | CICD / テスト / 04 | `spring-boot-cicd` |
   | AWS / クラウド / 05 | `spring-cloud-aws` |
   | 応用 / EC サイト / 07 | `spring-boot-webapp-practical` |
   | DB アクセス / JDBC・JPA・MyBatis / 09 | `java-db-access` |

   - 「コース 1 つだけの状態で」と講座を指定されなかったら、どれで試すかを 1 回だけ聞く
     （迷うなら order が先頭のもの）。
   - 該当が曖昧（例:「Spring のコース」）なら候補を挙げて確かめる。推測で決めない。

3. **起動する**。Claude の端末は対話入力ができないので、**必ず引数を付ける**
   （付けないと聞かずに全コースで起動する）。Electron はウィンドウを閉じるまで戻らないので
   **バックグラウンドで実行**する。

   ```bash
   cd desktop && npm start -- all                               # 統合版
   cd desktop && npm start -- spring-advanced                   # 1 コース
   cd desktop && npm start -- spring-advanced,java-db-access    # 複数コース（カンマ区切り）
   ```

   - 出力の `[start] 読み込むコース: ...` で、渡したコースが正しいことを確かめて伝える。
   - エディタバンドルのビルド（`build:editor`）が先に走る。エラーが出たらそこで止めて報告する。
   - すでに Codinable が起動していても別ウィンドウとして立ち上がる。前のものを閉じるかはユーザーに任せる。

4. **ユーザーに伝える**: どのコースで起動したか、画面で見るところ
   （ヘッダー左の 🎓 コース選択は 1 コースなら表示だけ・2 つ以上なら選べる。
   設定画面の「インストールされている講座」に「開発用の絞り込み」が出る）。

## 画面の振る舞いを自動で確かめたいとき

「1 コース / 2 コース / 全コースで画面がどうなるか確かめて」と言われたら、起動せずに次を実行する。

```bash
cd desktop && node test/check-course-filter.js
```

1 コースでコース選択が選べないこと、2 コースで選べて切り替えで演習一覧が入れ替わること、
全コースで `courses/` のコースがすべて並ぶことを見る。`すべて期待どおり` で終われば通過。

## 注意

- ユーザーが自分の端末で `npm start` を引数なしで実行した場合は、番号で選ぶ一覧が出る
  （Enter だけなら全コース）。その使い方を聞かれたら README.md の「起動とビルド」を案内する。
- `CODINABLE_COURSES` を直接設定しても同じ効果になるが、指示では `npm start -- <指定>` を使う。
- コースの中身（course.yaml / templates）を変更する作業はこのスキルの範囲外。
