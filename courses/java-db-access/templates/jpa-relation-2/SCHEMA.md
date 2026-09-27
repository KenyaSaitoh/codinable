# この演習で使うテーブル

「1対1・多対多と結合表」のコードが読み書きするテーブルです。
テーブルはインメモリの HSQLDB に作られます（定義: `sql/hsqldb/2_COMPANY_DDL.sql`、初期データ: `sql/hsqldb/3_COMPANY_DML.sql`）。

<!-- この説明は desktop/scripts/build-schema-docs.js が DDL から作っている。
     テーブルを変えたら、手で直さずに node scripts/build-schema-docs.js で作り直すこと -->

## PROJECT（プロジェクト）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `PROJECT_ID` | `INT` | 主キー | プロジェクトID |
| `PROJECT_NAME` | `VARCHAR(30)` | NOT NULL | プロジェクト名 |
| `PLATFORM` | `VARCHAR(30)` |  | プラットフォーム |

初期データ（4 件）

| PROJECT_ID | PROJECT_NAME | PLATFORM |
|---|---|---|
| 1 | 受発注管理システム | クライアントサーバ |
| 2 | 会計システム | パッケージ |
| 3 | オンラインショッピング | Spring Boot |
| 4 | 勤務管理システム | Spring Boot |

## QUALIFICATION（資格）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `QUALIFICATION_ID` | `INT` | 主キー | 資格ID |
| `QUALIFICATION_NAME` | `VARCHAR(30)` | NOT NULL | 資格名 |
| `QUALIFICATION_TYPE` | `VARCHAR(30)` |  | 資格種別 |

初期データ（7 件）

| QUALIFICATION_ID | QUALIFICATION_NAME | QUALIFICATION_TYPE |
|---|---|---|
| 11 | 基本情報技術者 | 情報処理技術者試験 |
| 12 | 応用情報技術者 | 情報処理技術者試験 |
| 13 | プロジェクトマネージャ | 情報処理技術者試験 |
| 21 | Java Silver | Oracle認定資格 |
| 22 | Java Gold | Oracle認定資格 |
| 31 | Oracle Silver | Oracle認定資格 |
| 32 | Oracle Gold | Oracle認定資格 |

## ADDRESS（住所）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `ADDRESS_ID` | `INT` | 主キー | 住所ID |
| `ZIP_CODE` | `VARCHAR(10)` |  | 郵便番号 |
| `PREFECTURE` | `VARCHAR(10)` |  | 都道府県 |
| `CITY` | `VARCHAR(20)` |  | 市町村 |

初期データ（14 件。先頭 12 件）

| ADDRESS_ID | ZIP_CODE | PREFECTURE | CITY |
|---|---|---|---|
| 1 | 132-0000 | 東京都 | 江戸川1-1-1 |
| 2 | 279-0000 | 千葉県 | 浦安市1-1-1 |
| 3 | 270-1300 | 千葉県 | 印西市1-1-1 |
| 4 | 221-0000 | 神奈川県 | 横浜市1-1-1 |
| 5 | 184-0000 | 東京都 | 小金井市1-1-1 |
| 6 | 182-0000 | 東京都 | 調布市1-1-1 |
| 7 | 220-0000 | 神奈川県 | 横浜市1-1-1 |
| 8 | 273-0000 | 千葉県 | 船橋市1-1-1 |
| 9 | 340-0000 | 埼玉県 | 草加市1-1-1 |
| 10 | 210-0000 | 神奈川県 | 川崎市1-1-1 |
| 11 | 331-0000 | 埼玉県 | さいたま市1-1-1 |
| 12 | 120-0000 | 東京都 | 足立区1-1-1 |

## JOB（役職）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `JOB_ID` | `INT` | 主キー | 役職ID |
| `JOB_NAME` | `VARCHAR(30)` | NOT NULL | 役職名 |

初期データ（4 件）

| JOB_ID | JOB_NAME |
|---|---|
| 1 | マネージャ |
| 2 | プロジェクトリーダ |
| 3 | システムエンジニア |
| 4 | プログラマ |

## DEPARTMENT（部署）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `DEPARTMENT_ID` | `INT` | 主キー | 部署ID |
| `DEPARTMENT_NAME` | `VARCHAR(30)` | NOT NULL | 部署名 |
| `LOCATION` | `VARCHAR(50)` | NOT NULL | 所在地 |

初期データ（4 件）

| DEPARTMENT_ID | DEPARTMENT_NAME | LOCATION |
|---|---|---|
| 1 | PLANNING | TOKYO HQ |
| 2 | HR | TOKYO HQ |
| 3 | SALES | YOKOHAMA BRANCH |
| 4 | PRODUCT | YOKOHAMA BRANCH |

## EMPLOYEE（社員）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー | 社員ID |
| `EMPLOYEE_NAME` | `VARCHAR(30)` | NOT NULL | 社員名 |
| `DEPARTMENT_ID` | `INT` | 外部キー → `DEPARTMENT.DEPARTMENT_ID` | 部署ID |
| `ENTRANCE_DATE` | `DATE` | NOT NULL | 入社年月日 |
| `ADDRESS_ID` | `INT` | 外部キー → `ADDRESS.ADDRESS_ID` | 住所ID |
| `JOB_ID` | `INT` | 外部キー → `JOB.JOB_ID`、NOT NULL | 役職ID |
| `SALARY` | `INT` | NOT NULL | 月給 |

初期データ（14 件。先頭 12 件）

| EMPLOYEE_ID | EMPLOYEE_NAME | DEPARTMENT_ID | ENTRANCE_DATE | ADDRESS_ID | JOB_ID | SALARY |
|---|---|---|---|---|---|---|
| 10001 | Alice | 1 | 2012-04-01 | 1 | 1 | 500000 |
| 10002 | Bob | 3 | 2012-04-01 | 2 | 1 | 450000 |
| 10003 | Carol | 2 | 2012-04-01 | 3 | 2 | 350000 |
| 10004 | Dave | 1 | 2012-04-01 | 4 | 1 | 400000 |
| 10005 | Ellen | 3 | 2013-04-01 | 5 | 3 | 300000 |
| 10006 | Frank | 1 | 2013-10-01 | 6 | 3 | 250000 |
| 10007 | Ivan | 3 | 2014-01-01 | 7 | 1 | 480000 |
| 10008 | Justin | 2 | 2014-04-01 | 8 | 1 | 460000 |
| 10009 | Mallory | 2 | 2014-07-01 | 9 | 2 | 420000 |
| 10010 | Matilda | 3 | 2015-08-01 | 10 | 3 | 280000 |
| 10011 | Oscar | 1 | 2015-11-01 | 11 | 3 | 320000 |
| 10012 | Pat | 2 | 2016-04-01 | 12 | 4 | 240000 |

## PHONE（電話）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `HOLDER_ID` | `INT` | 主キー、外部キー → `EMPLOYEE.EMPLOYEE_ID` | 所有者ID |
| `HOME_PHONE_NUMBER` | `VARCHAR(30)` |  | 自宅電話ID |
| `MOBILE_PHONE_NUMBER` | `VARCHAR(30)` |  | 携帯電話ID |

初期データ（12 件）

| HOLDER_ID | HOME_PHONE_NUMBER | MOBILE_PHONE_NUMBER |
|---|---|---|
| 10001 | 03-XXXX-XXXX | 090-XXXX-XXXX |
| 10002 | 047-XXX-XXXX | 080-XXXX-XXXX |
| 10003 | 0476-XX-XXXX | 090-XXXX-XXXX |
| 10004 | 045-XXX-XXXX | 080-XXXX-XXXX |
| 10005 | 0422-XX-XXXX |  |
| 10006 |  | 080-XXXX-XXXX |
| 10007 | 0424-XX-XXXX |  |
| 10009 | 047-XXX-XXXX | 090-XXXX-XXXX |
| 10010 |  | 080-XXXX-XXXX |
| 10011 |  | 090-XXXX-XXXX |
| 10012 | 044-XXX-XXXX | 080-XXXX-XXXX |
| 10013 | 048-XXX-XXXX | 090-XXXX-XXXX |

## EMAIL（メール）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMAIL_ID` | `INT` | 主キー | メールID |
| `HOLDER_ID` | `INT` | 外部キー → `EMPLOYEE.EMPLOYEE_ID` | 所有者ID |
| `ADDRESS` | `VARCHAR(30)` | NOT NULL | メールアドレス |

初期データ（28 件。先頭 12 件）

| EMAIL_ID | HOLDER_ID | ADDRESS |
|---|---|---|
| 1 | 10001 | alice@gmail.com |
| 2 | 10001 | alice@outlook.jp |
| 3 | 10001 | alice@yahoo.co.jp |
| 4 | 10002 | bob@gmail.com |
| 5 | 10002 | bob@outlook.jp |
| 6 | 10003 | carol@gmail.com |
| 7 | 10003 | carol@outlook.jp |
| 8 | 10004 | dave@gmail.com |
| 9 | 10004 | dave@outlook.jp |
| 10 | 10005 | ellen@gmail.com |
| 11 | 10006 | frank@gmail.com |
| 12 | 10006 | frank@outlook.jp |

## EMPLOYEE_PROJECT（社員プロジェクト関係）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー（複合）、外部キー → `EMPLOYEE.EMPLOYEE_ID` | 社員ID |
| `PROJECT_ID` | `INT` | 主キー（複合）、外部キー → `PROJECT.PROJECT_ID` | プロジェクトID |

初期データ（12 件）

| EMPLOYEE_ID | PROJECT_ID |
|---|---|
| 10005 | 1 |
| 10005 | 3 |
| 10006 | 2 |
| 10006 | 4 |
| 10009 | 1 |
| 10009 | 3 |
| 10010 | 1 |
| 10010 | 2 |
| 10010 | 3 |
| 10011 | 2 |
| 10014 | 3 |
| 10014 | 4 |

## EMPLOYEE_QUALIFICATION（社員資格関係）

| カラム | 型 | 制約 | 意味 |
|---|---|---|---|
| `EMPLOYEE_ID` | `INT` | 主キー（複合）、外部キー → `EMPLOYEE.EMPLOYEE_ID` | 社員ID |
| `QUALIFICATION_ID` | `INT` | 主キー（複合）、外部キー → `QUALIFICATION.QUALIFICATION_ID` | 資格ID |

初期データ（15 件。先頭 12 件）

| EMPLOYEE_ID | QUALIFICATION_ID |
|---|---|
| 10001 | 11 |
| 10001 | 12 |
| 10001 | 13 |
| 10003 | 13 |
| 10007 | 11 |
| 10007 | 31 |
| 10008 | 31 |
| 10009 | 21 |
| 10009 | 22 |
| 10009 | 31 |
| 10009 | 32 |
| 10011 | 11 |
