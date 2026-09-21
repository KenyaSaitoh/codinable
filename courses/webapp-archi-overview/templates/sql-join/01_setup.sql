-- 結合を試すための準備。まずこのファイルを流す
--
-- ここまでの演習では、EMPLOYEE が部署名を文字列でそのまま持っていた
-- その形だと同じ部署名が何度も重複し、部署名が変わったときに全件を直す必要がある
--
-- そこで部署を別のテーブルに分け、社員側は部署 ID で参照する形にする
-- この参照の仕組みが外部キー（FOREIGN KEY）であり、
-- 分けたテーブルを 1 つの結果にまとめて読むのが結合（JOIN）である

-- 参照している側（EMPLOYEE）から先に消す
-- 逆順だと「参照されているテーブルは消せない」と言われる
DROP TABLE EMPLOYEE IF EXISTS CASCADE;
DROP TABLE DEPARTMENT IF EXISTS CASCADE;

-- ── 部署テーブル（参照される側・親）─────────────────
CREATE TABLE DEPARTMENT (
    DEPARTMENT_ID   INT PRIMARY KEY,
    DEPARTMENT_NAME VARCHAR(30) NOT NULL,
    LOCATION        VARCHAR(30) NOT NULL
);

-- ── 社員テーブル（参照する側・子）───────────────────
CREATE TABLE EMPLOYEE (
    EMPLOYEE_ID   INT PRIMARY KEY,
    EMPLOYEE_NAME VARCHAR(30) NOT NULL,
    -- 部署テーブルの主キーを指す外部キー
    -- DEPARTMENT に無い ID は入れられない（参照整合性）
    DEPARTMENT_ID INT,
    ENTRANCE_DATE DATE NOT NULL,
    SALARY        INT  NOT NULL,
    FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT(DEPARTMENT_ID)
);

INSERT INTO DEPARTMENT VALUES (1, '営業部', '本社');
INSERT INTO DEPARTMENT VALUES (2, '企画部', '本社');
INSERT INTO DEPARTMENT VALUES (3, '人事部', '新宿支社');
-- 社員が 1 人もいない部署。外部結合の違いを見るために入れてある
INSERT INTO DEPARTMENT VALUES (4, '監査室', '新宿支社');

INSERT INTO EMPLOYEE VALUES (10001, 'Alice', 1, DATE '2018-04-01', 500000);
INSERT INTO EMPLOYEE VALUES (10002, 'Bob',   2, DATE '2019-04-01', 450000);
INSERT INTO EMPLOYEE VALUES (10003, 'Carol', 3, DATE '2020-10-01', 350000);
INSERT INTO EMPLOYEE VALUES (10004, 'Dave',  1, DATE '2021-04-01', 400000);
-- どの部署にも所属していない社員。これも外部結合の違いを見るために入れてある
INSERT INTO EMPLOYEE VALUES (10005, 'Eve', NULL, DATE '2026-04-01', 300000);

-- ── 存在しない部署 ID は入れられない ─────────────────
-- 次の 1 行の先頭の -- を外して 1 文だけ実行すると、外部キー違反のエラーになる
-- INSERT INTO EMPLOYEE VALUES (10006, 'Frank', 99, DATE '2026-04-01', 300000)

SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
