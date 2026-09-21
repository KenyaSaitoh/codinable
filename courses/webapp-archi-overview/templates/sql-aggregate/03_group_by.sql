-- グルーピング（GROUP BY）と集約結果の絞り込み（HAVING）
-- 01_setup.sql を流したあとに実行する

-- ── 部署ごとの人数 ──────────────────────────────────
-- GROUP BY に書いたカラムの値が同じ行が 1 つのグループにまとめられ、
-- グループごとに集約関数が適用される
SELECT DEPARTMENT_NAME, COUNT(*) AS 人数
  FROM EMPLOYEE
 GROUP BY DEPARTMENT_NAME
 ORDER BY DEPARTMENT_NAME;

-- ── 部署ごとの平均月給 ──────────────────────────────
SELECT DEPARTMENT_NAME,
       COUNT(*)    AS 人数,
       AVG(SALARY) AS 平均月給,
       MAX(SALARY) AS 最高月給
  FROM EMPLOYEE
 GROUP BY DEPARTMENT_NAME
 ORDER BY 平均月給 DESC;

-- ── SELECT 句に書けるもの ───────────────────────────
-- 書けるのは GROUP BY に挙げたカラムと、集約関数の結果だけである
-- 次の 1 文は EMPLOYEE_NAME がグループに 1 つに定まらないためエラーになる
-- （先頭の -- を外して 1 文だけ選択して実行すると、エラーが確かめられる）
-- SELECT DEPARTMENT_NAME, EMPLOYEE_NAME, COUNT(*) FROM EMPLOYEE GROUP BY DEPARTMENT_NAME

-- ── 集約前に絞る（WHERE）──────────────────────────
-- WHERE は行を絞る。ここでは 35 万円以上の社員だけを対象に集計している
SELECT DEPARTMENT_NAME, COUNT(*) AS 人数
  FROM EMPLOYEE
 WHERE 350000 <= SALARY
 GROUP BY DEPARTMENT_NAME
 ORDER BY DEPARTMENT_NAME;

-- ── 集約後に絞る（HAVING）─────────────────────────
-- HAVING はグループを絞る。2 人以上いる部署だけを残す
SELECT DEPARTMENT_NAME, COUNT(*) AS 人数
  FROM EMPLOYEE
 GROUP BY DEPARTMENT_NAME
HAVING 2 <= COUNT(*)
 ORDER BY 人数 DESC;

-- ── WHERE と HAVING を同時に使う ────────────────────
-- 先に WHERE で行を絞り、集約したあと HAVING でグループを絞る
SELECT DEPARTMENT_NAME, AVG(SALARY) AS 平均月給
  FROM EMPLOYEE
 WHERE DEPARTMENT_NAME IS NOT NULL
 GROUP BY DEPARTMENT_NAME
HAVING 400000 <= AVG(SALARY)
 ORDER BY 平均月給 DESC;

-- ── 複数のカラムでグルーピングする ──────────────────
-- 「部署 × 月給 40 万円以上か」の組み合わせごとに数える
SELECT DEPARTMENT_NAME,
       CASE WHEN 400000 <= SALARY THEN '40万以上' ELSE '40万未満' END AS 区分,
       COUNT(*) AS 人数
  FROM EMPLOYEE
 GROUP BY DEPARTMENT_NAME,
       CASE WHEN 400000 <= SALARY THEN '40万以上' ELSE '40万未満' END
 ORDER BY DEPARTMENT_NAME, 区分;
