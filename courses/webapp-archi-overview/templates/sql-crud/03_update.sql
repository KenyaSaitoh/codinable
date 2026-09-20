-- 更新（UPDATE）。01_setup.sql を流したあとに実行する。
--
-- UPDATE と DELETE は WHERE 句を書き忘れると全件が対象になる。
-- 実務では、同じ WHERE で SELECT して対象を確かめてから実行する。

-- ── 更新前の状態を見る ──────────────────────────────
SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;

-- ── 1 件更新（主キーを指定する）──────────────────────
-- 主キーは一意なので、対象は必ず 1 件に限られる
UPDATE EMPLOYEE SET SALARY = 550000 WHERE EMPLOYEE_ID = 10001;

-- ── 複数のカラムを同時に更新する ────────────────────
UPDATE EMPLOYEE
   SET DEPARTMENT_NAME = '企画部',
       SALARY          = 480000
 WHERE EMPLOYEE_ID = 10004;

-- ── 条件指定による更新（複数件が対象になる）─────────────
-- まず対象を確かめる
SELECT * FROM EMPLOYEE WHERE DEPARTMENT_NAME = '営業部' ORDER BY EMPLOYEE_ID;

-- 更新前の値を使って計算できる。5% の昇給。
-- SALARY は INT なので、小数になった結果を CAST で整数に戻している
UPDATE EMPLOYEE
   SET SALARY = CAST(SALARY * 1.05 AS INT)
 WHERE DEPARTMENT_NAME = '営業部';

-- ── NULL を入れる・NULL を埋める ─────────────────────
UPDATE EMPLOYEE SET DEPARTMENT_NAME = NULL WHERE EMPLOYEE_ID = 10003;

-- 部署が未設定の社員をまとめて埋める（IS NULL で絞る）
UPDATE EMPLOYEE SET DEPARTMENT_NAME = '総務部' WHERE DEPARTMENT_NAME IS NULL;

-- ── WHERE を付け忘れるとどうなるか ───────────────────
-- 次の 1 行の先頭の -- を外して実行すると、全員の月給が同じ値になる。
-- 元に戻すには 01_setup.sql をもう一度流す
-- UPDATE EMPLOYEE SET SALARY = 1

-- 更新後の状態を確かめる
SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
