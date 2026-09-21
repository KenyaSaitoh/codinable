import { useState, type FormEvent } from 'react';
import { DEPARTMENTS, type Department } from './types';

/*
 * 入力フォーム
 *
 * 自分の入力途中の値だけを state に持ち、確定したら親から渡された
 * onAdd を呼ぶ。「親から props で受け取り、親へは関数で知らせる」のが
 * React での親子のやりとり
 */
interface Props {
  onAdd: (name: string, department: Department) => void;
}

export default function PersonForm({ onAdd }: Props) {
  const [name, setName] = useState('');
  const [department, setDepartment] = useState<Department>(DEPARTMENTS[0]);

  const handleSubmit = (event: FormEvent) => {
    // 既定の送信 (ページ再読み込み) を止める。SPA ではページを移動しない
    event.preventDefault();

    const trimmed = name.trim();
    if (!trimmed) return;

    onAdd(trimmed, department);
    setName('');
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* JSX では class ではなく className と書く */}
      <label htmlFor="name">名前</label>
      <input id="name" type="text" value={name} onChange={e => setName(e.target.value)} />

      <label htmlFor="department">部署</label>
      <select
        id="department"
        value={department}
        onChange={e => setDepartment(e.target.value as Department)}
      >
        {/* 配列から要素を作るときは key を付ける。React が差分を追うために使う */}
        {DEPARTMENTS.map(d => (
          <option key={d} value={d}>
            {d}
          </option>
        ))}
      </select>

      <button type="submit">追加する</button>
    </form>
  );
}
