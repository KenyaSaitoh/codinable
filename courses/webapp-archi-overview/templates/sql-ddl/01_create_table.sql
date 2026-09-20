-- DDL（データ定義言語）。テーブルそのものを作る・変える・消す。
--
-- ここまでの演習は「テーブルがある前提」でデータを操作してきた（DML）。
-- この演習では、その入れ物のほうを作る。

-- 同じ名前のテーブルが残っていれば消す。
-- IF EXISTS を付けると、無いときにエラーにならない
DROP TABLE PRODUCT IF EXISTS CASCADE;

-- ── テーブルの作成 ──────────────────────────────────
-- 「カラム名 データ型 制約」の順に、カンマ区切りで並べる
CREATE TABLE PRODUCT (
    -- 整数。主キーなので一意で NULL を許さない
    PRODUCT_ID   INT         PRIMARY KEY,
    -- 可変長の文字列（最大 40 文字）。値が無い状態を許さない
    PRODUCT_NAME VARCHAR(40) NOT NULL,
    -- 同じ値を許さない（商品コードの重複を防ぐ）
    PRODUCT_CODE CHAR(8)     UNIQUE,
    -- 小数を保持する型。全体 10 桁のうち小数 2 桁
    PRICE        DECIMAL(10, 2) NOT NULL,
    -- 値が入っていなければ 0 を使う
    STOCK        INT         DEFAULT 0 NOT NULL,
    -- 条件を満たさない値を弾く
    RATING       INT         CHECK (RATING BETWEEN 1 AND 5),
    -- 真偽値
    AVAILABLE    BOOLEAN     DEFAULT TRUE NOT NULL,
    -- 日付と日時
    RELEASE_DATE DATE,
    CREATED_AT   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── データを入れてみる ──────────────────────────────
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, PRICE, STOCK, RATING, RELEASE_DATE)
VALUES (1, 'ノート', 'NB-00001', 480.00, 120, 4, DATE '2025-04-01');

INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, PRICE, STOCK, RATING, RELEASE_DATE)
VALUES (2, '万年筆', 'FP-00001', 3200.50, 8, 5, DATE '2025-06-15');

-- STOCK と AVAILABLE を省くと、DEFAULT に書いた値が入る
INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_CODE, PRICE)
VALUES (3, 'インク', 'IK-00001', 1100.00);

-- ── 定義どおりに入ったかを見る ──────────────────────
-- STOCK が 0、AVAILABLE が TRUE、CREATED_AT に現在時刻が入っている行がある
SELECT * FROM PRODUCT ORDER BY PRODUCT_ID;
