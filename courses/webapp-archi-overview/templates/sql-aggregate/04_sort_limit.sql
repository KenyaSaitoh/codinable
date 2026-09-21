-- 並べ替え（ORDER BY）・重複排除（DISTINCT）・件数制限（LIMIT）
-- 01_setup.sql を流したあとに実行する

-- ── 降順に並べる ────────────────────────────────────
SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE ORDER BY SALARY DESC;

-- ── 昇順に並べる（ASC は既定なので省略できる）──────────
SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE ORDER BY SALARY;

-- ── 複数のカラムで並べる ────────────────────────────
-- 部署名の昇順、同じ部署の中では月給の降順
SELECT DEPARTMENT_NAME, EMPLOYEE_NAME, SALARY
  FROM EMPLOYEE
 ORDER BY DEPARTMENT_NAME ASC, SALARY DESC;

-- ── NULL はどこに来るか ─────────────────────────────
-- 並び順での NULL の扱いは製品によって違う。明示したいときは NULLS FIRST / LAST を付ける
SELECT EMPLOYEE_NAME, DEPARTMENT_NAME
  FROM EMPLOYEE
 ORDER BY DEPARTMENT_NAME NULLS FIRST;

-- ── 重複を取り除く ──────────────────────────────────
-- 同じ部署名が何度も出てくるので、種類だけを知りたいときに使う
SELECT DISTINCT DEPARTMENT_NAME FROM EMPLOYEE ORDER BY DEPARTMENT_NAME;

-- ── 先頭から一定件数だけ取る ────────────────────────
-- 一覧画面のページ分割で使う。ORDER BY と組み合わせないと、
-- どの 3 件が返るかが決まらない
SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE ORDER BY SALARY DESC LIMIT 3;

-- ── 途中から取る（2 ページ目）───────────────────────
-- OFFSET で読み飛ばす件数を指定する
SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE ORDER BY SALARY DESC LIMIT 3 OFFSET 3;

-- ── 集計と組み合わせる ──────────────────────────────
-- 平均月給の高い部署から 2 つだけ
SELECT DEPARTMENT_NAME, AVG(SALARY) AS 平均月給
  FROM EMPLOYEE
 WHERE DEPARTMENT_NAME IS NOT NULL
 GROUP BY DEPARTMENT_NAME
 ORDER BY 平均月給 DESC
 LIMIT 2;
