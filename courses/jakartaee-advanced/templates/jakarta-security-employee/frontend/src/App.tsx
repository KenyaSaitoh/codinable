import { FormEvent, useEffect, useState } from 'react';
import * as api from './api';

// 社員管理の画面。ロールは表示の判断にだけ使い、変更操作の許可はサーバーの認可で判定する
// （Alice でも「登録」は押せる。押すとサーバーが 403 を返す）
export default function App() {
  const [session, setSession] = useState<api.Session | null>(null);
  const [employees, setEmployees] = useState<api.Employee[]>([]);
  const [message, setMessage] = useState('');
  const [username, setUsername] = useState('Bob');
  const [password, setPassword] = useState('2222');
  const [draft, setDraft] = useState({ employeeName: 'Walter', departmentId: 3, jobName: 'ASSOCIATE', salary: 230000 });

  const describe = (e: unknown) =>
    e instanceof api.HttpError
      ? { 401: '401 未認証です', 403: '403 拒否されました（ロールまたは CSRF トークン）' }[e.status] ?? e.message
      : String(e);

  const reload = async () => {
    try {
      setSession(await api.session());
      setEmployees(await api.employees());
    } catch (e) {
      setSession(null);
      setEmployees([]);
      if (!(e instanceof api.HttpError && e.status === 401)) setMessage(describe(e));
    }
  };

  useEffect(() => { void reload(); }, []);

  const onLogin = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api.login(username, password);
      setMessage(`${username} でログインしました（204）`);
      await reload();
    } catch (err) {
      setMessage(`ログインに失敗しました: ${describe(err)}`);
    }
  };

  const onLogout = async () => {
    await api.logout().catch(() => undefined);
    setMessage('ログアウトしました');
    await reload();
  };

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    try {
      const created = await api.createEmployee({ ...draft, entranceDate: null });
      setMessage(`登録しました（201）: ${created.employeeId} ${created.employeeName}`);
      await reload();
    } catch (err) {
      setMessage(`登録できません: ${describe(err)}`);
    }
  };

  const onDelete = async (id: number) => {
    try {
      await api.deleteEmployee(id);
      setMessage(`削除しました（204）: ${id}`);
      await reload();
    } catch (err) {
      setMessage(`削除できません: ${describe(err)}`);
    }
  };

  return (
    <main>
      <h1>社員管理（Jakarta Security）</h1>
      {message && <p className="message">{message}</p>}
      {session ? (
        <section className="session">
          <span>ログイン中: {session.username}（ロール: {session.roles.join(', ')}）</span>
          <button onClick={onLogout}>ログアウト</button>
        </section>
      ) : (
        <form className="login" onSubmit={onLogin}>
          <label>ユーザー名 <input value={username} onChange={(e) => setUsername(e.target.value)} /></label>
          <label>パスワード <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
          <button type="submit">ログイン</button>
          <span className="hint">Alice / 1111（BASIC）、Bob / 2222（ADMIN）</span>
        </form>
      )}
      {session && (
        <>
          <form className="create" onSubmit={onCreate}>
            <label>社員名 <input value={draft.employeeName} onChange={(e) => setDraft({ ...draft, employeeName: e.target.value })} /></label>
            <label>部署ID <input type="number" value={draft.departmentId} onChange={(e) => setDraft({ ...draft, departmentId: Number(e.target.value) })} /></label>
            <label>職種 <input value={draft.jobName} onChange={(e) => setDraft({ ...draft, jobName: e.target.value })} /></label>
            <label>給与 <input type="number" value={draft.salary} onChange={(e) => setDraft({ ...draft, salary: Number(e.target.value) })} /></label>
            <button type="submit">登録</button>
          </form>
          <table>
            <thead>
              <tr><th>社員ID</th><th>社員名</th><th>部署</th><th>職種</th><th>給与</th><th>入社日</th><th></th></tr>
            </thead>
            <tbody>
              {employees.map((e) => (
                <tr key={e.employeeId}>
                  <td>{e.employeeId}</td><td>{e.employeeName}</td><td>{e.departmentName ?? '（なし）'}</td>
                  <td>{e.jobName}</td><td>{e.salary.toLocaleString()}</td><td>{e.entranceDate ?? ''}</td>
                  <td><button onClick={() => onDelete(e.employeeId)}>削除</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </main>
  );
}
