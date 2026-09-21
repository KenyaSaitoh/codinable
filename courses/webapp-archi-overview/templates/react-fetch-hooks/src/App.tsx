import useUsers from './hooks/useUsers';

const App = () => {
  // 表示側は、データをどう取得するかを知らなくてよい
  const { users, loading, error, reload } = useUsers();

  return (
    <main>
      <p className="eyebrow">useEffect → fetch → setState → render</p>
      <h1>ユーザー一覧</h1>
      <button onClick={reload} disabled={loading}>再取得</button>

      <section aria-live="polite">
        {loading && <p className="status">読み込み中...</p>}
        {error && <p className="error">エラー: {error}</p>}
        {!loading && !error && (
          <ul>
            {users.map((user) => (
              <li key={user.id}>
                <strong>{user.name}</strong>
                <span>{user.email}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
};

export default App;
