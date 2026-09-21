import UserCard, { type User } from './UserCard';

const users: User[] = [
  { id: 1, name: 'Alice', email: 'alice@example.com', role: '設計' },
  { id: 2, name: 'Bob', email: 'bob@example.com', role: '開発' },
  { id: 3, name: 'Carol', email: 'carol@example.com', role: 'テスト' },
];

const App = () => (
  <main>
    <header>
      <p className="eyebrow">10.1.3 JSX / COMPONENTS</p>
      <h1>プロジェクトメンバー</h1>
      <p>同じUserCardを、異なるPropsで再利用しています。</p>
    </header>

    <section className="card-grid">
      {users.map((user) => (
        // 配列から描画する要素には、一意な key が必要
        <UserCard key={user.id} {...user} />
      ))}
    </section>
  </main>
);

export default App;
