-- テーブルの変更と削除。01_create_table.sql を流したあとに実行する

-- ── カラムを足す ────────────────────────────────────
-- 既にある行のその列は NULL になる（DEFAULT を付ければその値が入る）
ALTER TABLE PRODUCT ADD COLUMN CATEGORY VARCHAR(20);

-- 足したカラムに値を入れる
UPDATE PRODUCT SET CATEGORY = '文房具' WHERE PRODUCT_ID IN (1, 2, 3);

SELECT PRODUCT_ID, PRODUCT_NAME, CATEGORY FROM PRODUCT ORDER BY PRODUCT_ID;

-- ── カラムの定義を変える ────────────────────────────
-- 文字数を増やす方向は安全だが、減らす方向は既存データが入らなくなることがある
ALTER TABLE PRODUCT ALTER COLUMN CATEGORY VARCHAR(40);

-- ── カラムの名前を変える ────────────────────────────
ALTER TABLE PRODUCT ALTER COLUMN CATEGORY RENAME TO CATEGORY_NAME;

SELECT PRODUCT_ID, PRODUCT_NAME, CATEGORY_NAME FROM PRODUCT ORDER BY PRODUCT_ID;

-- ── カラムを消す ────────────────────────────────────
-- そのカラムのデータはすべて失われる
ALTER TABLE PRODUCT DROP COLUMN CATEGORY_NAME;

SELECT * FROM PRODUCT ORDER BY PRODUCT_ID;

-- ── テーブルを丸ごと消す ────────────────────────────
-- 中身のデータも定義も消える。本番環境では慎重に扱う操作である
-- ここでは使い捨てのテーブルを作って、消えることを確かめる
DROP TABLE SCRATCH IF EXISTS;

CREATE TABLE SCRATCH (
    ID   INT PRIMARY KEY,
    MEMO VARCHAR(20)
);

INSERT INTO SCRATCH VALUES (1, '消される予定');

SELECT * FROM SCRATCH;

DROP TABLE SCRATCH;

-- 消えたので、次の 1 行は実行できない
-- 先頭の -- を外してその 1 文だけを選択して実行すると、
-- 「テーブルが見つからない」エラーが確かめられる
-- SELECT * FROM SCRATCH

-- PRODUCT のほうは残っている
SELECT * FROM PRODUCT ORDER BY PRODUCT_ID;
