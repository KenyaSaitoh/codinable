-- 制約の確かめ方。01_create_table.sql を流したあとに実行する。
--
-- ここに並べた INSERT はすべてエラーになる。
-- 1 文ずつ選択して「実行」を押し、SQL タブに出るエラーを読む。
-- 「制約はデータベースが守ってくれる」ことを、失敗で確かめるのが目的である。
--
-- ファイル全体を実行すると、最初のエラーで止まる（それも正しい動きである）。

-- ── 主キーの重複（PRIMARY KEY 違反）─────────────────
-- PRODUCT_ID = 1 は既にある
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRICE)
VALUES (1, '重複した商品', 100.00);

-- ── NULL を許さないカラムに NULL（NOT NULL 違反）───────
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRICE)
VALUES (10, NULL, 100.00);

-- ── 必須カラムを省く（PRICE は NOT NULL で DEFAULT も無い）──
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME)
VALUES (11, '値段を書かない商品');

-- ── 重複を許さないカラムに同じ値（UNIQUE 違反）───────────
-- PRODUCT_CODE の NB-00001 は既にある
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, PRICE)
VALUES (12, '別の商品', 'NB-00001', 200.00);

-- ── 条件を満たさない値（CHECK 違反）──────────────────
-- RATING は 1〜5 しか入れられない
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRICE, RATING)
VALUES (13, '評価が範囲外の商品', 200.00, 9);

-- ── 型に合わない値 ──────────────────────────────────
-- PRICE は数値なので、文字列は入らない
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRICE)
VALUES (14, '値段が文字の商品', 'たかい');

-- ── 桁があふれる値 ──────────────────────────────────
-- PRODUCT_CODE は CHAR(8) なので 8 文字を超えると入らない
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, PRICE)
VALUES (15, 'コードが長い商品', 'CODE-TOO-LONG', 200.00);
