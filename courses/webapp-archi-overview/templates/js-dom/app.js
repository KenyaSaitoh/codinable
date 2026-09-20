// DOM 操作は、いつも次の 2 段階である。
//   1. 操作したい要素を取り出す
//   2. 取り出した要素のプロパティやメソッドを通じて書き換える
//
// 書き換えているのは Web ブラウザがメモリ上に持っている DOM ツリーであり、
// index.html というファイルは変わらない（プレビューを再読み込みすれば元に戻る）。

const log = document.getElementById('log');

/** 何をしたかを画面の下の一覧に残す（学習用。実務では console.log で十分） */
function note(text) {
  const li = document.createElement('li');
  li.textContent = text;
  log.appendChild(li);
}

// ── 1. id で 1 つ取り出す ──────────────────────────────
// getElementById は id 属性が一致する要素を 1 つ返す
const title = document.getElementById('title');
title.textContent = '書き換えたあとのタイトル';
note('getElementById("title") の textContent を差し替えた');

// ── 2. CSS セレクタで取り出す ──────────────────────────
// querySelector は CSS セレクタに最初に一致した要素を 1 つ返す
const message = document.querySelector('#message');
// textContent は文字として入る。タグを書いても文字のまま表示される
message.textContent = 'textContent に入れた文字は <b>タグも文字として</b> 扱われる';
note('querySelector("#message") の textContent を差し替えた');

// innerHTML は HTML として解釈される。
// 外部から来た文字（ユーザーの入力や API の応答）をそのまま入れてはいけない。
// スクリプトを混ぜ込まれる（XSS の入口になる）。
const card = document.querySelector('.card');
card.querySelector('p').innerHTML =
  'innerHTML なら <b>タグとして</b> 解釈される。外部から来た文字には使わない。';
note('querySelector(".card") の中の p を innerHTML で差し替えた');

// ── 3. まとめて取り出す ────────────────────────────────
// querySelectorAll は一致したすべての要素を返す。forEach で 1 つずつ扱う
const items = document.querySelectorAll('.list-item');
items.forEach((item, index) => {
  // style はインラインスタイル（HTML の style 属性）を直接触る
  item.style.color = index % 2 === 0 ? '#0969da' : '#57606a';
});
note(`querySelectorAll(".list-item") で ${items.length} 件に色を付けた`);

// ── 4. クラスの付け外し ───────────────────────────────
// 見た目は CSS 側に定義しておき、JavaScript はクラスを付け外しするだけにする。
// style を直接書くより、責務が分かれて見通しがよい。
card.classList.add('highlighted');
note('classList.add("highlighted") でカードを強調した');
// 他に classList.remove(...) / classList.toggle(...) / classList.contains(...) がある

// ── 5. 要素を作って入れる ─────────────────────────────
const people = [
  { name: 'Alice', department: '営業部' },
  { name: 'Bob',   department: '企画部' },
  { name: 'Carol', department: '人事部' },
];

const tbody = document.getElementById('person-list');
for (const person of people) {
  const row = document.createElement('tr');           // 要素を作る

  const name = document.createElement('td');
  name.textContent = person.name;

  const department = document.createElement('td');
  department.textContent = person.department;

  row.append(name, department);                        // 親に入れる
  tbody.appendChild(row);
}
note(`createElement と appendChild で ${people.length} 行を組み立てた`);

// ── 6. 属性を触る ────────────────────────────────────
// setAttribute / getAttribute で任意の属性を読み書きできる
title.setAttribute('title', 'マウスを乗せるとこの文が出る');
note('setAttribute("title", ...) でツールチップを付けた');
