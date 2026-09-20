// 振る舞いは JavaScript だけが担当する。
// データ (people) を正として持ち、変わったら画面を描き直す。
// この「状態 → 描画」の流れは、チャプター10のReactでも同じ考え方になる。

const people = [
  { name: 'Alice', department: '営業部' },
  { name: 'Bob',   department: '企画部' },
];

const form    = document.getElementById('person-form');
const list    = document.getElementById('person-list');
const count   = document.getElementById('count');
const message = document.getElementById('message');

function render() {
  list.innerHTML = '';
  people.forEach((person, index) => {
    const row = document.createElement('tr');

    const name = document.createElement('td');
    name.textContent = person.name;

    const department = document.createElement('td');
    department.textContent = person.department;

    const actions = document.createElement('td');
    const remove = document.createElement('button');
    remove.type = 'button';
    remove.textContent = '削除';
    remove.addEventListener('click', () => {
      people.splice(index, 1);
      render();
    });
    actions.appendChild(remove);

    row.append(name, department, actions);
    list.appendChild(row);
  });
  count.textContent = `${people.length} 件`;
}

form.addEventListener('submit', event => {
  // 既定の送信 (ページ再読み込み) を止めて、JavaScript で処理する
  event.preventDefault();

  const name = form.name.value.trim();
  if (!name) return;

  people.push({ name, department: form.department.value });
  form.reset();
  render();

  message.textContent = `${name} さんを追加しました`;
  setTimeout(() => { message.textContent = ''; }, 2000);
});

render();
