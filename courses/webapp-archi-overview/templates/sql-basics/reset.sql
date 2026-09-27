-- この演習の初期状態（テーブルの作り直しと初期データ）
--
-- 「実行」で SQL ファイルを流すと、Codinable が毎回その前にこのファイルを自動で流す
-- そのため、どのファイルも何度でも「初期データから」実行できる
-- （更新系の SQL を 2 回流して主キーが重複する、ということが起きない）
--
-- 範囲を選択して「実行」したときは流さない（1 文ずつ順に試せるように）
-- このファイルを自分で直すと、初期状態もそのとおりに変わる

DROP TABLE EMPLOYEE IF EXISTS CASCADE;

CREATE TABLE EMPLOYEE (
    EMPLOYEE_ID     INT,
    EMPLOYEE_NAME   VARCHAR(30) NOT NULL,
    DEPARTMENT_NAME VARCHAR(30),
    SALARY          INT NOT NULL,
    PRIMARY KEY(EMPLOYEE_ID)
);

INSERT INTO EMPLOYEE VALUES (10001, 'Alice', '営業部', 500000);

INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10002, 'Bob', '企画部', 450000);

INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10003, 'Carol', '人事部', 350000);

INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_NAME, SALARY)
VALUES (10004, 'Dave', '営業部', 400000);
