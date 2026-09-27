-- テーブルの作成と初期データの投入。まずこのファイルを流す
--
-- 「実行」を押すと、開いているこのファイルが HSQLDB に流れる
-- DB が止まっていれば自動で起動する
-- 一部だけ試したいときは、その範囲を選択してから「実行」を押す
--
-- どのファイルも、実行するたびに reset.sql（このファイルと同じテーブルと初期データ）が
-- 先に流れるので、いつも同じ 16 件から始まる
--
-- 講義と同じ EMPLOYEE テーブル（4 カラム）を作り、講義の 4 件（Alice〜Dave）を含む 16 件を入れる
-- 部署名をそのまま文字列で持つ形である
-- これを正規化して DEPARTMENT テーブルに分ける話は sql-join の演習で扱う

-- 同じ名前のテーブルが残っていれば消す
-- IF EXISTS は「無いときもエラーにしない」、CASCADE は「そのテーブルに依存する
-- ビューなども一緒に消す」という指定である（他の SQL 演習から移ってきても流せる）
DROP TABLE EMPLOYEE IF EXISTS CASCADE;

CREATE TABLE EMPLOYEE (
    EMPLOYEE_ID     INT,
    EMPLOYEE_NAME   VARCHAR(30) NOT NULL,
    DEPARTMENT_NAME VARCHAR(30),
    SALARY          INT NOT NULL,
    PRIMARY KEY(EMPLOYEE_ID)
);

-- カラム名を明示する書き方。テーブル定義の順序に依存しないので実務ではこちら
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10001, 'Alice', 'SALES', 500000);

-- カラム順に値を並べる書き方。短く書けるので、残りはこちらでまとめて入れる
INSERT INTO EMPLOYEE VALUES (10002, 'Bob',     'PLANNING', 450000);
INSERT INTO EMPLOYEE VALUES (10003, 'Carol',   'HR',       350000);
INSERT INTO EMPLOYEE VALUES (10004, 'Dave',    'SALES',    400000);
INSERT INTO EMPLOYEE VALUES (10005, 'Ellen',   'SALES',    300000);
INSERT INTO EMPLOYEE VALUES (10006, 'Frank',   'PLANNING', 250000);
INSERT INTO EMPLOYEE VALUES (10007, 'Ivan',    'PRODUCT',  480000);
INSERT INTO EMPLOYEE VALUES (10008, 'Justin',  'HR',       460000);
INSERT INTO EMPLOYEE VALUES (10009, 'Mallory', 'PRODUCT',  420000);
INSERT INTO EMPLOYEE VALUES (10010, 'Matilda', 'SALES',    280000);
INSERT INTO EMPLOYEE VALUES (10011, 'Oscar',   'PRODUCT',  320000);
INSERT INTO EMPLOYEE VALUES (10012, 'Pat',     'PRODUCT',  240000);
INSERT INTO EMPLOYEE VALUES (10013, 'Peggy',   'SALES',    270000);
-- 部署が決まっていない社員。部署名は NULL（値が無い状態）にしておく
INSERT INTO EMPLOYEE VALUES (10014, 'Victor',  NULL,       220000);
INSERT INTO EMPLOYEE VALUES (10015, 'Steve',   'PLANNING', 380000);
INSERT INTO EMPLOYEE VALUES (10016, 'Trent',   'PRODUCT',  310000);

-- 最後の 1 文の結果が SQL タブに出る。16 件そろっていることを確かめる
SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
