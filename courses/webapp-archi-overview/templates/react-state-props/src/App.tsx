import { useState } from 'react';
import UserList, { type User } from './UserList';

const initialUsers: User[] = [
  { id: 1, name: 'Alice' },
  { id: 2, name: 'Bob' },
  { id: 3, name: 'Carol' },
];

const App = () => {
  // Stateの所有者は親。setterを通して更新するとReactが再描画する。
  const [users, setUsers] = useState(initialUsers);
  const [count, setCount] = useState(0);

  const deleteUser = (id: number) => {
    setUsers((current) => current.filter((user) => user.id !== id));
  };

  return (
    <main>
      <h1>StateとProps</h1>
      <section>
        <h2>Stateだけを持つカウンター</h2>
        <p className="count">{count}</p>
        <div className="buttons">
          <button onClick={() => setCount((value) => value - 1)}>-1</button>
          <button onClick={() => setCount((value) => value + 1)}>+1</button>
        </div>
      </section>

      <section>
        <h2>親から子へ流れるProps</h2>
        <p>AppのState: {users.length}件 → UserListのProps</p>
        <UserList users={users} onDelete={deleteUser} />
        <button className="reset" onClick={() => setUsers(initialUsers)}>初期状態へ戻す</button>
      </section>
    </main>
  );
};

export default App;
