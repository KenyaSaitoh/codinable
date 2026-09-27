-- CRUD（登録・参照・更新・削除）を試すための準備
--
-- この演習は EMPLOYEE テーブルの中身を実際に書き換える
-- ここに書いたテーブルと初期データは reset.sql と同じもので、
-- どのファイルを実行しても、その前に reset.sql が流れて元の 4 件に戻る
-- （何度実行しても、主キーの重複でエラーになることはない）

DROP TABLE EMPLOYEE IF EXISTS CASCADE;

CREATE TABLE EMPLOYEE (
    EMPLOYEE_ID     INT,
    EMPLOYEE_NAME   VARCHAR(30) NOT NULL,
    DEPARTMENT_NAME VARCHAR(30),
    SALARY          INT NOT NULL,
    PRIMARY KEY(EMPLOYEE_ID)
);

INSERT INTO EMPLOYEE VALUES (10001, 'Alice', '営業部', 500000);
INSERT INTO EMPLOYEE VALUES (10002, 'Bob',   '企画部', 450000);
INSERT INTO EMPLOYEE VALUES (10003, 'Carol', '人事部', 350000);
INSERT INTO EMPLOYEE VALUES (10004, 'Dave',  '営業部', 400000);

SELECT * FROM EMPLOYEE ORDER BY EMPLOYEE_ID;
