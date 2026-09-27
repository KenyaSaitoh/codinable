# 集計と整形で使うテーブル

この演習では、社員を表す **EMPLOYEE** テーブルを使います。
集計は件数が少ないと違いが見えないので、講義の 4 件（Alice〜Dave）を含む **16 件**を入れてあります。

## 動かし方

エディタ下の実行対象のセレクトから SQL ファイルを選び、「実行」を押します。
流した SQL がエディタに開き、結果は **SQL タブ**に表で出ます。

| 順番 | ファイル | 内容 |
|---|---|---|
| 1 | `01_setup.sql` | テーブルを作り直し、16 件の社員を入れる（`reset.sql` と同じ内容） |
| 2 | `02_functions.sql` | 集約関数（COUNT / SUM / AVG / MAX / MIN） |
| 3 | `03_group_by.sql` | グルーピング（GROUP BY）と集約後の絞り込み（HAVING） |
| 4 | `04_sort_limit.sql` | 並べ替え（ORDER BY）・重複排除（DISTINCT）・件数制限（LIMIT） |

- 結果が出るのは最後の 1 文だけです。途中の文を見たいときは、その 1 文を選択してから「実行」を押します
- どのファイルも、「実行」するたびに `reset.sql` が先に流れて、テーブルが作り直され初期データに戻ります。何度実行しても同じ状態から始まります
- 範囲を選択して「実行」したときは作り直しません（1 文ずつ順に試せるように）

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員番号 |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 氏名 |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | | 部署名（グルーピングの単位） |
| `SALARY` | `INT` | NOT NULL | 月給（円。集計の対象） |

### 初期データ（`reset.sql`）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_NAME | SALARY |
|---|---|---|---|
| 10001 | Alice | SALES | 500000 |
| 10002 | Bob | PLANNING | 450000 |
| 10003 | Carol | HR | 350000 |
| 10004 | Dave | SALES | 400000 |
| 10005 | Ellen | SALES | 300000 |
| 10006 | Frank | PLANNING | 250000 |
| 10007 | Ivan | PRODUCT | 480000 |
| 10008 | Justin | HR | 460000 |
| 10009 | Mallory | PRODUCT | 420000 |
| 10010 | Matilda | SALES | 280000 |
| 10011 | Oscar | PRODUCT | 320000 |
| 10012 | Pat | PRODUCT | 240000 |
| 10013 | Peggy | SALES | 270000 |
| 10014 | Victor | *(NULL)* | 220000 |
| 10015 | Steve | PLANNING | 380000 |
| 10016 | Trent | PRODUCT | 310000 |

部署ごとの人数は SALES 5・PLANNING 3・HR 2・PRODUCT 5・部署なし 1 です。
部署が NULL の Victor は、COUNT や GROUP BY が NULL をどう扱うかを見るために入れてあります。
