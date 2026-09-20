-- 結合（JOIN）。01_setup.sql を流したあとに実行する。
--
-- 分けた 2 つのテーブルを、1 つの結果にまとめて読む。
-- 内部結合と外部結合の違いは「対応する行が無い側をどう扱うか」だけである。

-- ── 内部結合: 両方に対応する行があるものだけ ───────────
-- E と D はテーブルの別名。長いテーブル名を繰り返さずに済む。
-- ON にはつなぐ条件（外部キー = 主キー）を書く
SELECT E.EMPLOYEE_NAME, D.DEPARTMENT_NAME, D.LOCATION, E.SALARY
  FROM EMPLOYEE E
 INNER JOIN DEPARTMENT D ON E.DEPARTMENT_ID = D.DEPARTMENT_ID
 ORDER BY E.EMPLOYEE_ID;
-- 部署が未設定の Eve は出てこない（対応する部署が無いため）

-- ── 左外部結合: 左側は漏らさない ────────────────────
-- 左（EMPLOYEE）の全件を返し、対応する部署が無ければ NULL で埋める。
-- Eve の部署名が NULL で出てくる
SELECT E.EMPLOYEE_NAME, D.DEPARTMENT_NAME
  FROM EMPLOYEE E
  LEFT JOIN DEPARTMENT D ON E.DEPARTMENT_ID = D.DEPARTMENT_ID
 ORDER BY E.EMPLOYEE_ID;

-- ── 左右を入れ替えると見えるものが変わる ────────────────
-- 部署を左に置くと、社員が 1 人もいない監査室も出てくる
SELECT D.DEPARTMENT_NAME, E.EMPLOYEE_NAME
  FROM DEPARTMENT D
  LEFT JOIN EMPLOYEE E ON D.DEPARTMENT_ID = E.DEPARTMENT_ID
 ORDER BY D.DEPARTMENT_ID, E.EMPLOYEE_ID;

-- ── 結合してから絞る ────────────────────────────────
SELECT E.EMPLOYEE_NAME, D.DEPARTMENT_NAME, E.SALARY
  FROM EMPLOYEE E
 INNER JOIN DEPARTMENT D ON E.DEPARTMENT_ID = D.DEPARTMENT_ID
 WHERE D.LOCATION = '本社'
   AND 400000 <= E.SALARY
 ORDER BY E.SALARY DESC;

-- ── 結合してから集計する ────────────────────────────
-- 部署ごとの人数と平均月給。社員のいない部署も残したいので LEFT JOIN にする。
-- COUNT(E.EMPLOYEE_ID) はカラム指定なので、NULL の行を数えない（監査室が 0 人になる）
SELECT D.DEPARTMENT_NAME,
       COUNT(E.EMPLOYEE_ID) AS 人数,
       AVG(E.SALARY)        AS 平均月給
  FROM DEPARTMENT D
  LEFT JOIN EMPLOYEE E ON D.DEPARTMENT_ID = E.DEPARTMENT_ID
 GROUP BY D.DEPARTMENT_NAME
 ORDER BY 人数 DESC, D.DEPARTMENT_NAME;

-- ── 3 つ以上のテーブルをつなぐ ──────────────────────
-- JOIN は並べて書ける。ここでは部署を 2 回使って「同じ所在地の同僚」を出している
SELECT E.EMPLOYEE_NAME AS 社員, D.LOCATION AS 所在地, E2.EMPLOYEE_NAME AS 同じ所在地の社員
  FROM EMPLOYEE E
 INNER JOIN DEPARTMENT D  ON E.DEPARTMENT_ID = D.DEPARTMENT_ID
 INNER JOIN DEPARTMENT D2 ON D.LOCATION = D2.LOCATION
 INNER JOIN EMPLOYEE E2   ON D2.DEPARTMENT_ID = E2.DEPARTMENT_ID
 WHERE E.EMPLOYEE_ID <> E2.EMPLOYEE_ID
 ORDER BY E.EMPLOYEE_ID, E2.EMPLOYEE_ID;
