// JavaScript の基本。実行対象で「開いているファイル」を選んで実行する

// ── 変数: 再代入しないものは const、するものは let。var は使わない ──
const name = 'Alice';
let count = 0;
count += 1;
console.log(name, count);

// ── テンプレートリテラル ──
console.log(`${name} さんは ${count} 回目の登録です`);

// ── 関数: アロー関数が基本。関数も値として扱える ──
const add = (a, b) => a + b;
console.log('add(10, 20) =', add(10, 20));

// ── 配列の高階関数: for を回さず、やりたいことの名前で書く ──
const people = [
  { name: 'Alice', department: '営業部', salary: 500000 },
  { name: 'Bob',   department: '企画部', salary: 450000 },
  { name: 'Carol', department: '人事部', salary: 350000 },
  { name: 'Dave',  department: '営業部', salary: 400000 },
];

console.log('map:', people.map(p => p.name));
console.log('filter:', people.filter(p => p.salary >= 450000).map(p => p.name));
console.log('reduce:', people.reduce((sum, p) => sum + p.salary, 0));
console.log('find:', people.find(p => p.department === '人事部'));

// ── 分割代入とスプレッド構文 ──
const [first, second] = people;
console.log('分割代入:', first.name, second.name);

const { name: firstName, department } = first;
console.log('オブジェクトの分割代入:', firstName, department);

// 元の配列・オブジェクトは書き換えず、新しいものを作る
const added = [...people, { name: 'Eve', department: '企画部', salary: 380000 }];
const raised = { ...first, salary: first.salary + 50000 };
console.log('スプレッド:', added.length, raised);

// ── オプショナルチェーンと Null 合体演算子 ──
const maybe = people.find(p => p.name === 'Zoe');
console.log('見つからないとき:', maybe?.department ?? '(未所属)');

// ── グループ化: Map を使って部署ごとにまとめる ──
const byDepartment = new Map();
for (const person of people) {
  if (!byDepartment.has(person.department)) byDepartment.set(person.department, []);
  byDepartment.get(person.department).push(person.name);
}
for (const [dept, names] of byDepartment) {
  console.log(`${dept}: ${names.join(', ')}`);
}
