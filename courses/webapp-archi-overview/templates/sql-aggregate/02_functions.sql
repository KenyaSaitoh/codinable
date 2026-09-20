-- 集約関数。01_setup.sql を流したあとに実行する。
--
-- 集約関数は「複数のレコードから 1 つの値を出す」関数である。
-- 結果は最後の 1 文だけが出るので、1 文ずつ選択して実行すると見比べやすい。

-- ── COUNT: 件数 ─────────────────────────────────────
SELECT COUNT(*) AS 全件数 FROM EMPLOYEE;

-- 条件に合う件数
SELECT COUNT(*) AS 月給40万以上 FROM EMPLOYEE WHERE 400000 <= SALARY;

-- カラム名を指定すると、そのカラムが NULL の行は数えない。
-- 全件数（9）との差が、部署未設定の 1 件である
SELECT COUNT(*) AS 全件数, COUNT(DEPARTMENT_NAME) AS 部署あり FROM EMPLOYEE;

-- 値の種類を数える（DISTINCT との組み合わせ）
SELECT COUNT(DISTINCT DEPARTMENT_NAME) AS 部署の数 FROM EMPLOYEE;

-- ── SUM: 合計 ───────────────────────────────────────
SELECT SUM(SALARY) AS 月給合計 FROM EMPLOYEE;

SELECT SUM(SALARY) AS 営業部の合計 FROM EMPLOYEE WHERE DEPARTMENT_NAME = '営業部';

-- ── AVG: 平均 ───────────────────────────────────────
-- SALARY は INT なので、平均も整数に丸められる。
-- 小数で見たいときは、集計する前に小数の型へ変換する
SELECT AVG(SALARY) AS 平均_整数,
       AVG(CAST(SALARY AS DECIMAL(12, 2))) AS 平均_小数
  FROM EMPLOYEE;

-- ── MAX / MIN: 最大・最小 ───────────────────────────
SELECT MAX(SALARY) AS 最高, MIN(SALARY) AS 最低 FROM EMPLOYEE;

-- 文字列にも使える（辞書順の最大・最小）
SELECT MAX(EMPLOYEE_NAME) AS 名前の最大, MIN(EMPLOYEE_NAME) AS 名前の最小 FROM EMPLOYEE;

-- ── まとめて並べる ──────────────────────────────────
-- 集約関数は SELECT 句の中でカラムのように書ける
SELECT COUNT(*)    AS 人数,
       SUM(SALARY) AS 合計,
       AVG(SALARY) AS 平均,
       MIN(SALARY) AS 最小,
       MAX(SALARY) AS 最大
  FROM EMPLOYEE;
