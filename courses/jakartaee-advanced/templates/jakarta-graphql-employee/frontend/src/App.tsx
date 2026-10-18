import { FormEvent, useEffect, useState } from 'react';
import GraphqlPanel from './components/GraphqlPanel';
import * as api from './services/api';

// 社員管理の画面（GraphQL）。一覧・詳細・部署ごとで、同じ /graphql へ異なる項目を要求する
export default function App() {
  const [employees, setEmployees] = useState<api.Employee[]>([]);
  const [detail, setDetail] = useState<api.Employee | null>(null);
  const [detailId, setDetailId] = useState('10001');
  const [message, setMessage] = useState('');
  const [draft, setDraft] = useState({ employeeName: 'Walter', departmentId: '3', jobName: 'ASSOCIATE', salary: 230000 });

  const reload = async () => {
    try {
      setEmployees(await api.employees());
    } catch (e) {
      setMessage(String(e));
    }
  };

  useEffect(() => { void reload(); }, []);

  const onDetail = async (e: FormEvent) => {
    e.preventDefault();
    try {
      setDetail(await api.employeeDetail(detailId));
      setMessage('');
    } catch (err) {
      setDetail(null);
      setMessage(`errors: ${err}`);
    }
  };

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    try {
      const created = await api.createEmployee(draft);
      setMessage(`登録しました: ${created.id} ${created.employeeName}`);
      await reload();
    } catch (err) {
      setMessage(`errors: ${err}`);
    }
  };

  const onDelete = async (id: string) => {
    try {
      setMessage(`削除しました: ${await api.deleteEmployee(id)}`);
      await reload();
    } catch (err) {
      setMessage(`errors: ${err}`);
    }
  };

  return (
    <main>
      <h1>社員管理（MicroProfile GraphQL）</h1>
      {message && <p className="message">{message}</p>}

      <form onSubmit={onDetail}>
        <label>社員ID <input value={detailId} onChange={(e) => setDetailId(e.target.value)} /></label>
        <button type="submit">詳細（EmployeeDetail）</button>
        {detail && <span>{detail.id} {detail.employeeName}（{detail.department?.departmentName ?? '部署なし'}）</span>}
      </form>

      <GraphqlPanel />

      <h2>社員一覧</h2>
      <form onSubmit={onCreate}>
        <label>社員名 <input value={draft.employeeName} onChange={(e) => setDraft({ ...draft, employeeName: e.target.value })} /></label>
        <label>部署ID <input value={draft.departmentId} onChange={(e) => setDraft({ ...draft, departmentId: e.target.value })} /></label>
        <label>職種 <input value={draft.jobName} onChange={(e) => setDraft({ ...draft, jobName: e.target.value })} /></label>
        <label>給与 <input type="number" value={draft.salary} onChange={(e) => setDraft({ ...draft, salary: Number(e.target.value) })} /></label>
        <button type="submit">登録（createEmployee）</button>
      </form>
      <table>
        <thead>
          <tr><th>id</th><th>社員名</th><th>部署</th><th>職種</th><th>給与</th><th>入社日</th><th></th></tr>
        </thead>
        <tbody>
          {employees.map((e) => (
            <tr key={e.id}>
              <td>{e.id}</td><td>{e.employeeName}</td><td>{e.department?.departmentName ?? '（なし）'}</td>
              <td>{e.jobName}</td><td>{e.salary?.toLocaleString()}</td><td>{e.entranceDate ?? ''}</td>
              <td><button onClick={() => onDelete(e.id)}>削除</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  );
}
