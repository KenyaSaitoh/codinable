-- カスケード。親のレコードが消えたとき、子のレコードをどう扱うかの指定である。
--
-- このファイルは 01_setup.sql とは別のテーブル（PROJECT / ASSIGNMENT）を作るので、
-- 単独で実行できる。

DROP TABLE ASSIGNMENT IF EXISTS CASCADE;
DROP TABLE PROJECT    IF EXISTS CASCADE;

-- 親テーブル
CREATE TABLE PROJECT (
    PROJECT_ID   INT PRIMARY KEY,
    PROJECT_NAME VARCHAR(30) NOT NULL
);

-- 子テーブル。親が消えたら、この行も一緒に消す指定を付ける
CREATE TABLE ASSIGNMENT (
    ASSIGNMENT_ID INT PRIMARY KEY,
    PROJECT_ID    INT,
    MEMBER_NAME   VARCHAR(30) NOT NULL,
    FOREIGN KEY (PROJECT_ID) REFERENCES PROJECT(PROJECT_ID) ON DELETE CASCADE
);

INSERT INTO PROJECT VALUES (1, '販売管理刷新');
INSERT INTO PROJECT VALUES (2, '社内ポータル');

INSERT INTO ASSIGNMENT VALUES (101, 1, 'Alice');
INSERT INTO ASSIGNMENT VALUES (102, 1, 'Bob');
INSERT INTO ASSIGNMENT VALUES (103, 2, 'Carol');

-- ── 削除前 ──────────────────────────────────────────
SELECT A.ASSIGNMENT_ID, P.PROJECT_NAME, A.MEMBER_NAME
  FROM ASSIGNMENT A
 INNER JOIN PROJECT P ON A.PROJECT_ID = P.PROJECT_ID
 ORDER BY A.ASSIGNMENT_ID;

-- ── 親を 1 件消す ───────────────────────────────────
-- ON DELETE CASCADE を付けてあるので、このプロジェクトの担当 2 件も一緒に消える
DELETE FROM PROJECT WHERE PROJECT_ID = 1;

-- 残っているのは社内ポータルの 1 件だけ
SELECT * FROM ASSIGNMENT ORDER BY ASSIGNMENT_ID;

-- ── 指定しなかった場合（既定は RESTRICT）─────────────
-- 何も書かなければ、子が残っている親は消せない。
-- 他の選択肢は次のとおりである。
--   ON DELETE CASCADE   … 子も一緒に消す（この演習の指定）
--   ON DELETE SET NULL  … 子の外部キーを NULL にする
--   ON DELETE RESTRICT  … 子が残っていれば親を消せない（既定）
--
-- 01_setup.sql の EMPLOYEE は何も指定していないので RESTRICT である。
-- 01_setup.sql を流したあと、次の 1 行の先頭の -- を外して実行すると、
-- 社員が所属している部署は消せないことが確かめられる
-- DELETE FROM DEPARTMENT WHERE DEPARTMENT_ID = 1

SELECT P.PROJECT_NAME, COUNT(A.ASSIGNMENT_ID) AS 担当件数
  FROM PROJECT P
  LEFT JOIN ASSIGNMENT A ON P.PROJECT_ID = A.PROJECT_ID
 GROUP BY P.PROJECT_NAME
 ORDER BY P.PROJECT_NAME;
