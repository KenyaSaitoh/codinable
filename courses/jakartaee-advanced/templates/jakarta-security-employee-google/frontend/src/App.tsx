import { useEffect, useState } from 'react';
import * as api from './api';

// 社員管理の画面（Google 版）。Google で認証された利用者のグループは BASIC だけなので、削除はサーバーが 403 で拒否する
export default function App() {
  const [session, setSession] = useState<api.Session | null>(null);
  const [employees, setEmployees] = useState<api.Employee[]>([]);
  const [message, setMessage] = useState('');

  const describe = (e: unknown) =>
    e instanceof api.HttpError
      ? { 401: '401 未認証です', 403: '403 拒否されました（ADMIN ロールが必要です）' }[e.status] ?? e.message
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

  const onLogout = async () => {
    await api.logout().catch(() => undefined);
    setMessage('ログアウトしました（Google 側のログイン状態は残ります）');
    await reload();
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
      <h1>社員管理（Google による OpenID Connect）</h1>
      {message && <p className="message">{message}</p>}
      {session ? (
        <section className="session">
          <span>ログイン中: {session.name}（email: {session.email || 'なし'}、ロール: {session.roles.join(', ')}）</span>
          <button onClick={onLogout}>ログアウト</button>
        </section>
      ) : (
        <section className="session">
          <a className="button" href={api.LOGIN_URL}>Googleでログイン</a>
          <span className="hint">先に payara/post-boot-commands.asadmin に Google のクライアントIDとシークレットを書く（README 参照）</span>
        </section>
      )}
      {session && (
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
      )}
    </main>
  );
}
