// Fetch API は、Web ブラウザから HTTP リクエストを送るための標準の仕組みである
//
// fetch は「すぐには返らない処理」なので、async / await と組み合わせて書く
// await を付けると、応答が返るまでこの関数の続きが待たされる（画面は固まらない）
// 非同期処理そのものの詳しい話は 7 章で扱う

const status = document.getElementById('status');
const tbody  = document.getElementById('user-list');

async function loadUsers(url) {
  status.textContent = '読み込み中…';
  status.className   = 'status';
  tbody.innerHTML    = '';

  try {
    // ① リクエストを送り、応答（ヘッダー）が返るまで待つ
    const response = await fetch(url);

    // ② 404 や 500 でも fetch は例外にならない。状態は自分で確かめる
    //    （通信自体が失敗したときだけ例外になる）
    if (!response.ok) {
      status.textContent = `失敗した（HTTP ${response.status} ${response.statusText}）`;
      status.className   = 'status error';
      return;
    }

    // ③ 本文を JSON として読む。ここも待つ必要がある
    const users = await response.json();

    // ④ 受け取ったデータで、ページの一部だけを組み立て直す
    render(users);
    status.textContent = `${users.length} 件を表示した（ページは再読み込みしていない）`;
    status.className   = 'status ok';

  } catch (error) {
    // サーバーに届かない、応答が JSON でない、などはここに来る
    status.textContent = `通信エラー: ${error.message}`;
    status.className   = 'status error';
  }
}

function render(users) {
  tbody.innerHTML = '';
  for (const user of users) {
    const row = document.createElement('tr');
    for (const value of [user.id, user.name, user.department, user.salary.toLocaleString('ja-JP')]) {
      const cell = document.createElement('td');
      cell.textContent = value;
      row.appendChild(cell);
    }
    tbody.appendChild(row);
  }
}

document.getElementById('load').addEventListener('click', () => {
  // 相対パス。index.html と同じ場所の users.json を指す
  loadUsers('users.json');
});

document.getElementById('load-broken').addEventListener('click', () => {
  // 存在しないパス。404 が返ってくる
  loadUsers('no-such-file.json');
});

// 最初の 1 回は自動で読み込む
loadUsers('users.json');

// サーバーへ送るときは method と headers、body を指定する
// 静的サーバーは受け取れないのでここでは動かないが、形は次のとおりである
// 実際に動かすのは 8 章（サーバーサイド）と 9 章（Web API）の演習で扱う
//
//   const response = await fetch('/api/users', {
//     method:  'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body:    JSON.stringify({ name: 'Eve', department: '営業部', salary: 380000 }),
//   });
