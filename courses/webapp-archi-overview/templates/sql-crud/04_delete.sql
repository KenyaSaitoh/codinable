-- 削除（DELETE）。01_setup.sql を流したあとに実行する
--
-- 消したデータは戻らない。元の 4 件に戻したいときは 01_setup.sql を流し直す

-- ── 消す前に対象を確かめる ──────────────────────────
-- 削除は「同じ WHERE で SELECT して件数を見てから」が定石である
SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID = 10003;

-- ── 1 件削除（主キーを指定する）──────────────────────
DELETE FROM EMPLOYEE WHERE EMPLOYEE_ID = 10003;

-- ── 条件指定による削除（複数件が対象になる）─────────────
-- まず対象を確かめる
SELECT * FROM EMPLOYEE WHERE SALARY < 450000 ORDER BY EMPLOYEE_ID;

-- 内容に問題がなければ実行する
DELETE FROM EMPLOYEE WHERE SALARY < 450000;

-- ── 残りを確かめる ──────────────────────────────────
SELECT COUNT(*) AS 残り件数 FROM EMPLOYEE;

-- ── WHERE を付けない DELETE は全件削除になる ───────────
-- 次の 1 行の先頭の -- を外して実行すると、テーブルが空になる
-- テーブル自体は残る（テーブルごと消すのは DROP TABLE で、sql-ddl の演習で扱う）
-- DELETE FROM EMPLOYEE

SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
