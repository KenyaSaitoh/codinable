-- テーブルの作成と初期データの投入。まずこのファイルを流す。
--
-- 「実行」を押すと、開いているこのファイルが HSQLDB に流れる。
-- DB が止まっていれば自動で起動する（SQL タブの「DB起動」を押してもよい）。
-- 一部だけ試したいときは、その範囲を選択してから「実行」を押す。
--
-- HSQLDB はインメモリなので、「DB停止」すると中身は消える。
-- おかしくなったら、このファイルをもう一度流せば元に戻る。
--
-- 講義と同じ EMPLOYEE テーブル（4 カラム・4 レコード）を作る。
-- 部署名をそのまま文字列で持つ形である。
-- これを正規化して DEPARTMENT テーブルに分ける話は sql-join の演習で扱う。

-- 同じ名前のテーブルが残っていれば消す。
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

-- カラム順に値を並べる書き方
INSERT INTO EMPLOYEE VALUES (10001, 'Alice', '営業部', 500000);

-- カラム名を明示する書き方。テーブル定義の順序に依存しないので実務ではこちら
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10002, 'Bob', '企画部', 450000);

INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10003, 'Carol', '人事部', 350000);

INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10004, 'Dave', '営業部', 400000);

-- 最後の 1 文の結果が SQL タブに出る。4 件そろっていることを確かめる
SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
