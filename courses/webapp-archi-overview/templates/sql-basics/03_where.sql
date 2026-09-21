-- 条件の書き方（WHERE 句）。01_create.sql を流したあとに実行する
--
-- 主キー検索と違い、条件検索の結果は 0 件・1 件・複数件のいずれにもなる
-- 1 文ずつ選択して「実行」を押すと、条件と結果の対応が見やすい

-- ── AND: 両方を満たすもの ───────────────────────────
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME = '営業部'
   AND 400000 <= SALARY
 ORDER BY EMPLOYEE_ID;

-- ── OR: どちらかを満たすもの ────────────────────────
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME = '人事部'
    OR 480000 <= SALARY
 ORDER BY EMPLOYEE_ID;

-- ── AND と OR を混ぜるときは括弧で意図を示す ──────────
-- 括弧が無いと AND が先に結び付くので、読み手が迷う条件になる
SELECT * FROM EMPLOYEE
 WHERE (DEPARTMENT_NAME = '営業部' OR DEPARTMENT_NAME = '企画部')
   AND 420000 <= SALARY
 ORDER BY EMPLOYEE_ID;

-- ── IN: 候補のどれかに一致するもの ──────────────────
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME IN ('営業部', '企画部')
 ORDER BY EMPLOYEE_ID;

-- NOT IN で「どれにも一致しないもの」
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME NOT IN ('営業部', '企画部')
 ORDER BY EMPLOYEE_ID;

-- ── BETWEEN: 範囲に含まれるもの（両端を含む）───────────
SELECT * FROM EMPLOYEE
 WHERE SALARY BETWEEN 300000 AND 450000
 ORDER BY SALARY;

-- ── LIKE: パターンに一致するもの ─────────────────────
-- % は任意の長さの文字列、_ は任意の 1 文字
SELECT * FROM EMPLOYEE WHERE EMPLOYEE_NAME LIKE 'A%' ORDER BY EMPLOYEE_ID;

-- 「部」で終わる部署（この例では全件が該当する）
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME LIKE '%部' ORDER BY EMPLOYEE_ID;

-- 4 文字の氏名（_ を 4 つ並べる）
SELECT * FROM EMPLOYEE WHERE EMPLOYEE_NAME LIKE '____' ORDER BY EMPLOYEE_ID;

-- ── NULL の扱い ─────────────────────────────────────
-- NULL は「値が無い」という状態で、= では判定できない。IS NULL を使う
-- まず部署が無い社員を 1 人入れて、動きを確かめる
INSERT INTO EMPLOYEE VALUES (10009, 'Frank', NULL, 300000);

-- = NULL では 1 件も取れない（NULL 同士は等しいと判定されないため）
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME = NULL;

-- IS NULL なら取れる
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME IS NULL;

-- 値が入っているものだけ
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME IS NOT NULL ORDER BY EMPLOYEE_ID;

-- 足した 1 件を消して、元の 4 件に戻す
DELETE FROM EMPLOYEE WHERE EMPLOYEE_ID = 10009;

SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
