-- 登録（INSERT）。01_setup.sql を流したあとに実行する。

-- ── カラム順に値を並べる ────────────────────────────
-- 文字列はシングルクォートで囲み、数値はそのまま書く
INSERT INTO EMPLOYEE VALUES (10005, 'Eve', '営業部', 380000);

-- ── カラム名を明示する（実務ではこちら）──────────────
-- テーブル定義の順序が変わっても書き換えずに済み、何を入れているかも読み取れる
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10006, 'Frank', '企画部', 420000);

-- ── 挙げなかったカラムは NULL になる ─────────────────
-- DEPARTMENT_NAME には NOT NULL を付けていないので、部署未定でも登録できる。
-- SALARY を省くと NOT NULL 違反でエラーになる（試すときは 1 文だけ選択して実行する）
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, SALARY)
VALUES (10007, 'Grace', 360000);

-- ── 検索結果をそのまま登録する ──────────────────────
-- VALUES の代わりに SELECT を書くと、取れた行をまとめて入れられる
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
SELECT EMPLOYEE_ID + 90000, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY
  FROM EMPLOYEE
 WHERE DEPARTMENT_NAME = '人事部';

-- ── 主キーの重複はエラーになる ──────────────────────
-- 次の 1 行の先頭の -- を外し、その 1 文だけを選択して実行すると、
-- 一意制約違反のエラーが SQL タブに出る。
-- これが「主キーがレコードを一意に識別する」ことの裏返しである
-- INSERT INTO EMPLOYEE VALUES (10001, 'Alice2', '営業部', 500000)

-- 登録された結果を確かめる
SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
