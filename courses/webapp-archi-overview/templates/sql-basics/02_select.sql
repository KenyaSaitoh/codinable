-- 検索（SELECT）の基本。01_create.sql を流したあとに実行する。
--
-- 結果は最後の 1 文だけが出るので、1 文ずつ選択して「実行」を押すと見比べやすい。

-- ── 主キー検索 ──────────────────────────────────────
-- 主キーは一意なので、結果は 0 件または 1 件にしかならない。
-- Web アプリケーションで詳細画面を出すときの、いちばん基本的な形である
SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID = 10001;

-- ── 全件・全カラム ──────────────────────────────────
-- * はすべてのカラムを指す。実務では必要なカラムだけ挙げるほうがよい
SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;

-- ── 必要なカラムだけ取り出す（射影）──────────────────
SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE ORDER BY EMPLOYEE_ID;

-- ── 比較演算子で絞る（選択）─────────────────────────
-- 使えるのは = <> < <= > >= の 6 つ
SELECT * FROM EMPLOYEE WHERE 400000 <= SALARY ORDER BY SALARY DESC;

-- 等しくない
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME <> '営業部' ORDER BY EMPLOYEE_ID;

-- ── 列に別名を付ける ────────────────────────────────
SELECT EMPLOYEE_NAME AS 氏名, SALARY / 10000 AS 月給_万円
  FROM EMPLOYEE
 ORDER BY SALARY DESC;
