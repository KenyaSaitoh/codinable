-- テスト用データ
DELETE FROM ORDER_DETAIL;
DELETE FROM ORDER_TRAN;
DELETE FROM STOCK;
DELETE FROM BOOK;
DELETE FROM CATEGORY;
DELETE FROM PUBLISHER;

INSERT INTO PUBLISHER (PUBLISHER_ID, PUBLISHER_NAME) VALUES
(1, 'デジタルフロンティア出版'),
(2, 'コードブレイクプレス'),
(3, 'ネットワークノード出版'),
(4, 'クラウドキャスティング社'),
(5, 'データドリフト社');

INSERT INTO CATEGORY (CATEGORY_ID, CATEGORY_NAME) VALUES
(1, 'Java'),
(2, 'SpringBoot'),
(3, 'SQL'),
(4, 'HTML/CSS'),
(5, 'JavaScript'),
(6, 'Python'),
(7, '生成AI'),
(8, 'クラウド'),
(9, 'AWS');

INSERT INTO BOOK (BOOK_ID, BOOK_NAME, AUTHOR, CATEGORY_ID, PUBLISHER_ID, PRICE) VALUES
(1,  'Java SEディープダイブ', 'Michael Johnson', 1, 3, 3400),
(2,  'JVMとバイトコードの探求', 'James Lopez', 1, 1, 4200),
(3,  'Javaアーキテクトのための設計原理', 'David Jones', 1, 4, 3000),
(9,  'SpringBoot in Cloud', 'Paul Martin', 2, 3, 3000),
(10, 'SpringBootによるエンタープライズ開発', 'Matthew Brown', 2, 2, 3900),
(13, 'SpringBootでのAPI実践', 'Steven Thomas', 2, 5, 3500),
(14, 'データベースの科学', 'Mark Jackson', 3, 4, 2500),
(17, 'SQLの冒険～RDBの深層', 'Brian Lee', 3, 2, 2200),
(24, 'ES6＋完全ガイド', 'Ryan Hill', 5, 5, 3000),
(26, 'JSアーキテクチャパターンの探求', 'Mark Jackson', 5, 4, 4200),
(30, 'Pythonプログラミング実践入門', 'Alice Carter', 6, 2, 3000),
(33, 'テスト自動化のためのPython', 'Daniel Moore', 6, 3, 3400);

INSERT INTO STOCK (BOOK_ID, QUANTITY, VERSION) VALUES
(1, 3, 0), (2, 2, 0), (3, 1, 0), (9, 2, 0), (10, 3, 0),
(13, 3, 0), (14, 3, 0), (17, 3, 0), (24, 3, 0), (26, 2, 0),
(30, 2, 0), (33, 0, 0);

INSERT INTO ORDER_TRAN (ORDER_TRAN_ID, ORDER_DATE, CUSTOMER_ID, TOTAL_PRICE, DELIVERY_PRICE, DELIVERY_ADDRESS, SETTLEMENT_TYPE) VALUES
(1, DATE '2023-03-01', 1, 5600, 500, '東京都中央区1-1-1', 1),
(2, DATE '2023-04-01', 1, 5700, 500, '東京都中央区1-1-1', 2);

INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, PRICE, COUNT) VALUES
(1, 1, 1, 3400, 1),
(1, 2, 17, 2200, 1),
(2, 1, 10, 3900, 1),
(2, 2, 24, 3000, 1);

