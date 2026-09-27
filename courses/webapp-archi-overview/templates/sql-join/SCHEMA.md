# 結合・サブクエリ・ビューで使うテーブル

ここまでの演習では、EMPLOYEE が部署名を文字列のまま持っていました。
この演習では部署を **DEPARTMENT** テーブルに分け、社員側は部署 ID で参照します。
この参照が外部キーで、分けたテーブルを 1 つの結果にまとめて読むのが結合（JOIN）です。

```
DEPARTMENT（部署・親）          EMPLOYEE（社員・子）
  DEPARTMENT_ID  ◀───────────  DEPARTMENT_ID（外部キー）
```

## 動かし方

エディタ下の実行対象のセレクトから SQL ファイルを選び、「実行」を押します。
流した SQL がエディタに開き、結果は **SQL タブ**に表で出ます。

| 順番 | ファイル | 内容 |
|---|---|---|
| 1 | `01_setup.sql` | DEPARTMENT と EMPLOYEE を作り、データを入れる（**最初に 1 回流す**） |
| 2 | `02_join.sql` | 内部結合と外部結合（対応する行が無い側の扱いの違い） |
| 3 | `03_subquery.sql` | サブクエリとビュー（`HIGH_SALARY_EMPLOYEES`） |
| 4 | `04_cascade.sql` | PROJECT / ASSIGNMENT を作り、親を消すと子も消える（ON DELETE CASCADE）ことを見る |

- 結果が出るのは最後の 1 文だけです。途中の文を見たいときは、その 1 文を選択してから「実行」を押します
- おかしくなったら `01_setup.sql` を流し直せば元に戻ります

## DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署番号 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(30)` | NOT NULL | 所在地 |

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION |
|---|---|---|
| 1 | 営業部 | 本社 |
| 2 | 企画部 | 本社 |
| 3 | 人事部 | 新宿支社 |
| 4 | 監査室 | 新宿支社 |

監査室には社員が 1 人もいません（外部結合の違いを見るため）。

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員番号 |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 氏名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 所属部署（DEPARTMENT に無い ID は入れられない） |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社日 |
| `SALARY` | `INT` | NOT NULL | 月給（円） |

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | ENTRANCE_DATE | SALARY |
|---|---|---|---|---|
| 10001 | Alice | 1 | 2018-04-01 | 500000 |
| 10002 | Bob | 2 | 2019-04-01 | 450000 |
| 10003 | Carol | 3 | 2020-10-01 | 350000 |
| 10004 | Dave | 1 | 2021-04-01 | 400000 |
| 10005 | Eve | *(NULL)* | 2026-04-01 | 300000 |

Eve はどの部署にも所属していません（これも外部結合の違いを見るため）。

## `04_cascade.sql` で作るテーブル

| テーブル | カラム |
|---|---|
| PROJECT（プロジェクト・親） | `PROJECT_ID INT` 主キー、`PROJECT_NAME VARCHAR(30)` NOT NULL |
| ASSIGNMENT（担当・子） | `ASSIGNMENT_ID INT` 主キー、`PROJECT_ID INT`（外部キー → PROJECT、**ON DELETE CASCADE**）、`MEMBER_NAME VARCHAR(30)` NOT NULL |
