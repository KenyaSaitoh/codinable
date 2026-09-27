-- 条件の書き方（WHERE 句）
--
-- 主キー検索と違い、条件検索の結果は 0 件・1 件・複数件のいずれにもなる
-- 1 文ずつ選択して「実行」を押すと、条件と結果の対応が見やすい

-- ── AND: 両方を満たすもの ───────────────────────────
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME = 'SALES'
   AND 400000 <= SALARY
 ORDER BY EMPLOYEE_ID;

-- ── OR: どちらかを満たすもの ────────────────────────
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME = 'HR'
    OR 480000 <= SALARY
 ORDER BY EMPLOYEE_ID;

-- ── AND と OR を混ぜるときは括弧で意図を示す ──────────
-- 括弧が無いと AND が先に結び付くので、読み手が迷う条件になる
SELECT * FROM EMPLOYEE
 WHERE (DEPARTMENT_NAME = 'SALES' OR DEPARTMENT_NAME = 'PLANNING')
   AND 420000 <= SALARY
 ORDER BY EMPLOYEE_ID;

-- ── IN: 候補のどれかに一致するもの ──────────────────
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME IN ('SALES', 'PLANNING')
 ORDER BY EMPLOYEE_ID;

-- NOT IN で「どれにも一致しないもの」
SELECT * FROM EMPLOYEE
 WHERE DEPARTMENT_NAME NOT IN ('SALES', 'PLANNING')
 ORDER BY EMPLOYEE_ID;

-- ── BETWEEN: 範囲に含まれるもの（両端を含む）───────────
SELECT * FROM EMPLOYEE
 WHERE SALARY BETWEEN 300000 AND 450000
 ORDER BY SALARY;

-- ── LIKE: パターンに一致するもの ─────────────────────
-- % は任意の長さの文字列、_ は任意の 1 文字
SELECT * FROM EMPLOYEE WHERE EMPLOYEE_NAME LIKE 'A%' ORDER BY EMPLOYEE_ID;

-- 「P」で始まる部署（PLANNING と PRODUCT が該当する）
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME LIKE 'P%' ORDER BY EMPLOYEE_ID;

-- 4 文字の氏名（_ を 4 つ並べる。Dave と Ivan が該当する）
SELECT * FROM EMPLOYEE WHERE EMPLOYEE_NAME LIKE '____' ORDER BY EMPLOYEE_ID;

-- ── NULL の扱い ─────────────────────────────────────
-- NULL は「値が無い」という状態で、= では判定できない。IS NULL を使う
-- 初期データの Victor（10014）は部署が決まっておらず、DEPARTMENT_NAME が NULL である
-- NOT IN の結果に Victor が出てこないのも、NULL はどの比較でも真にならないためである

-- = NULL では 1 件も取れない（NULL 同士は等しいと判定されないため）
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME = NULL;

-- IS NULL なら取れる
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME IS NULL;

-- 値が入っているものだけ（Victor 以外の 15 件）
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME IS NOT NULL ORDER BY EMPLOYEE_ID;
