import type { Person } from './types';

/*
 * 一覧表示。自分では state を持たず、渡されたものをそのまま描く。
 * こういうコンポーネントは動きが読みやすく、使い回しやすい。
 */
interface Props {
  people: Person[];
  onRemove: (id: number) => void;
}

export default function PersonList({ people, onRemove }: Props) {
  // 条件によって別のものを描きたいときは、早めに return する
  if (people.length === 0) {
    return <p className="empty">該当する人がいません</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>名前</th>
          <th>部署</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {people.map(person => (
          <tr key={person.id}>
            <td>{person.name}</td>
            <td>{person.department}</td>
            <td>
              {/* onClick には関数を渡す。onRemove(person.id) と書くと描画時に呼ばれてしまう */}
              <button type="button" onClick={() => onRemove(person.id)}>
                削除
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
