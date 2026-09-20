import { useMemo, useState } from 'react';
import PersonForm from './PersonForm';
import PersonList from './PersonList';
import type { Person } from './types';

/*
 * 画面全体をまとめるコンポーネント。
 *
 * React の考え方は「状態 (state) を書き換えると、それに応じて画面が描き直される」。
 * DOM を自分で組み立てないのが、チャプター3の静的ページとの一番大きな違い。
 */
export default function App() {
  // state はこの App が持ち、子には値と「変える手段」を渡す (単方向データフロー)
  const [people, setPeople] = useState<Person[]>([
    { id: 1, name: 'Alice', department: '営業部' },
    { id: 2, name: 'Bob', department: '企画部' },
  ]);
  const [keyword, setKeyword] = useState('');

  // 配列は書き換えず、新しい配列を作って渡す。
  // 元の配列を push で変えても React は変化に気づけない。
  const addPerson = (name: string, department: Person['department']) => {
    setPeople(prev => [...prev, { id: Math.max(0, ...prev.map(p => p.id)) + 1, name, department }]);
  };

  const removePerson = (id: number) => {
    setPeople(prev => prev.filter(p => p.id !== id));
  };

  // useMemo は、依存している値が変わらないかぎり計算結果を再利用する
  const visible = useMemo(
    () => people.filter(p => p.name.toLowerCase().includes(keyword.toLowerCase())),
    [people, keyword],
  );

  return (
    <div className="app">
      <header>
        <h1>名簿管理 (React)</h1>
      </header>

      <main>
        <section className="panel">
          <h2>登録</h2>
          <PersonForm onAdd={addPerson} />
        </section>

        <section className="panel">
          <h2>
            一覧 <span className="count">{visible.length} 件</span>
          </h2>
          <input
            className="search"
            type="search"
            placeholder="名前で絞り込む"
            value={keyword}
            // 入力欄の値を state に持たせ、state を唯一の正とする (制御コンポーネント)
            onChange={event => setKeyword(event.target.value)}
          />
          <PersonList people={visible} onRemove={removePerson} />
        </section>
      </main>
    </div>
  );
}
