// 名簿管理アプリケーションのサーバー
//
// 講座本編のサーバーは Jakarta EE の JAX-RS (PersonResource) だが、
// Codinable ではすぐ動かせるように、同じ URL と同じ JSON を返す REST API を Express で書いている
// 主役はクライアント側の public/app.js (jQuery) なので、こちらは読み流してよい
//
// ▶ アプリ実行を押すと、初回だけ依存パッケージ (express / jquery) を自動で入れてから起動し、
// http://localhost:3000 を検知してプレビュータブが開く

const express = require('express');
const path    = require('path');

const app  = express();
const port = 3000;

// JSON の本文 (Content-Type: application/json) を req.body に展開する
app.use(express.json());

// 画面 (public/) と、npm で入れた jQuery 本体を配信する
app.use(express.static(path.join(__dirname, 'public')));
app.use('/vendor/jquery', express.static(path.join(__dirname, 'node_modules/jquery/dist')));

// ── PersonMapper にあたる部分 ──
// Person の一覧をメモリ上のリストで持つだけの素朴な実装
// サーバーを再起動すると最初の 2 件に戻る
const persons = [
  { personId: 1, personName: 'Alice', age: 25, gender: 'female' },
  { personId: 2, personName: 'Bob',   age: 35, gender: 'male' },
];

const personMapper = {
  selectAll() {
    return persons;
  },
  // 最大の personId に 1 を足して採番し、採番済みの Person を返す
  insert(person) {
    const personId = Math.max(0, ...persons.map(p => p.personId)) + 1;
    const created = { ...person, personId };
    persons.push(created);
    return created;
  },
  update(person) {
    const index = persons.findIndex(p => p.personId === person.personId);
    if (index >= 0) persons[index] = person;
    return index >= 0;
  },
  delete(personId) {
    const index = persons.findIndex(p => p.personId === personId);
    if (index >= 0) persons.splice(index, 1);
    return index >= 0;
  },
};

// ── PersonResource にあたる部分 ──
// CRUD 操作と HTTP メソッドの対応は原稿のとおり
//   GET    /person_service/persons             一覧取得
//   POST   /person_service/persons             新規登録 (採番済みの Person を返す)
//   PUT    /person_service/persons/{personId}  更新
//   DELETE /person_service/persons/{personId}  削除
const PERSONS = '/person_service/persons';

app.get(PERSONS, (req, res) => {
  res.json(personMapper.selectAll());
});

app.post(PERSONS, (req, res) => {
  res.json(personMapper.insert(toPerson(req.body)));
});

app.put(`${PERSONS}/:personId`, (req, res) => {
  const person = { ...toPerson(req.body), personId: Number(req.params.personId) };
  res.sendStatus(personMapper.update(person) ? 204 : 404);
});

app.delete(`${PERSONS}/:personId`, (req, res) => {
  res.sendStatus(personMapper.delete(Number(req.params.personId)) ? 204 : 404);
});

// 受け取った JSON から Person の項目だけを取り出す (エンティティ Person にあたる)
function toPerson(body) {
  return {
    personId:   body.personId ?? null,
    personName: String(body.personName ?? ''),
    age:        Number(body.age),
    gender:     body.gender === 'female' ? 'female' : 'male',
  };
}

app.listen(port, () => {
  console.log(`名簿管理アプリケーション: http://localhost:${port}`);
  console.log(`REST API:                 http://localhost:${port}${PERSONS}`);
});
