// TypeScript。Node.js 24 以降は型注釈を書いたまま直接実行できる
// (実行時に型だけを取り除く type stripping。ビルド手順は要らない)
//
// 型は「実行前に間違いを見つけるための注釈」であり、実行時には消える

// ── 型エイリアスとインターフェース ──
type Department = 'SALES' | 'PLANNING' | 'HR' | 'PRODUCT';

interface Person {
  name: string;
  department: Department;
  salary: number;
  // ? を付けると省略できる
  email?: string;
}

const people: Person[] = [
  { name: 'Alice', department: 'SALES', salary: 500000, email: 'alice@example.com' },
  { name: 'Bob',   department: 'PLANNING', salary: 450000 },
  { name: 'Carol', department: 'HR', salary: 350000 },
];

// ── 引数と戻り値の型 ──
function totalSalary(members: Person[]): number {
  return members.reduce((sum, p) => sum + p.salary, 0);
}

console.log('合計:', totalSalary(people));

// ── ユニオン型の絞り込み ──
function label(value: string | number): string {
  // typeof で分岐すると、その枝の中では型が確定する
  if (typeof value === 'number') return `${value.toFixed(0)} 円`;
  return value.toUpperCase();
}

console.log(label(500000), label('alice'));

// ── ジェネリクス: 型を引数のように受け取る ──
function groupBy<T, K extends string>(items: T[], keyOf: (item: T) => K): Map<K, T[]> {
  const result = new Map<K, T[]>();
  for (const item of items) {
    const key = keyOf(item);
    if (!result.has(key)) result.set(key, []);
    result.get(key)!.push(item);
  }
  return result;
}

for (const [department, members] of groupBy(people, p => p.department)) {
  console.log(`${department}: ${members.map(m => m.name).join(', ')}`);
}

// ── 省略できる項目の扱い ──
for (const person of people) {
  // email は string | undefined なので、そのままでは使えない
  console.log(person.name, person.email ?? '(メールなし)');
}
