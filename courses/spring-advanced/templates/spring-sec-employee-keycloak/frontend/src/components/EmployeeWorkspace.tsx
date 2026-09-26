import { useState } from 'react';
import type { Chapter, Employee, Session } from '../types';
import { secured, readOnly } from '../chapters';
import { employees, saveEmployee, deleteEmployee } from '../services/api';
import { useTask } from './useTask';
import { Feedback } from './Feedback';
import EmployeeTable from './EmployeeTable';
import EmployeeForm from './EmployeeForm';
import SessionPanel from './SessionPanel';
import GraphqlPanel from './GraphqlPanel';
import StreamPanel from './StreamPanel';
export default function EmployeeWorkspace({ chapter }: { chapter: Chapter }) {
  const [rows, setRows] = useState<Employee[]>([]);
  const [session, setSession] = useState<Session | null>(null);
  const [editing, setEditing] = useState<Employee | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [id, setId] = useState('1');
  const [department, setDepartment] = useState('10');
  const [lower, setLower] = useState('0');
  const [upper, setUpper] = useState('500000');
  const task = useTask();
  const secure = secured(chapter.id);
  const writable = !readOnly(chapter.id) && (!secure || session?.authorities.includes('ADMIN'));
  const load = (filter = '', value = '') => task.run(async () => { setRows(await employees(chapter.id, filter, value)); });
  function onSession(value: Session | null) { setSession(value); setRows([]); setShowForm(false); }
  return <>
    {secure && <SessionPanel chapter={chapter.id} session={session} onSession={onSession} />}
    <section className="panel"><h2>社員一覧</h2>
      <div className="inline"><button disabled={task.busy} onClick={() => void load()}>全社員を取得</button>
        {writable && <button disabled={task.busy} onClick={() => { setEditing(null); setShowForm(true); }}>社員を登録</button>}
        <span className="muted">{rows.length} 件表示</span>
      </div>
      <form className="inline" onSubmit={(event) => { event.preventDefault(); void load('id', id); }}>
        <label>社員ID<input type="number" required min={1} value={id} onChange={(event) => setId(event.target.value)} /></label>
        <button disabled={task.busy}>ID検索</button>
      </form>
      {['rest', 'graphql'].includes(chapter.id) && <form className="inline" onSubmit={(event) => { event.preventDefault(); void load('department', department); }}>
        <label>検索する部署<select value={department} onChange={(event) => setDepartment(event.target.value)}><option value="10">営業部</option><option value="20">開発部</option><option value="30">総務部</option></select></label>
        <button disabled={task.busy}>部署検索</button>
      </form>}
      {['rest', 'webflux'].includes(chapter.id) && <form className="inline" onSubmit={(event) => { event.preventDefault(); void load('salary', new URLSearchParams({ lowerSalary: lower, upperSalary: upper }).toString()); }}>
        <label>給与の下限<input type="number" required min={0} value={lower} onChange={(event) => setLower(event.target.value)} /></label>
        <label>給与の上限<input type="number" required min={Number(lower)} value={upper} onChange={(event) => setUpper(event.target.value)} /></label>
        <button disabled={task.busy}>給与検索</button>
      </form>}
      <Feedback {...task} />
      <EmployeeTable rows={rows} edit={writable && !task.busy ? (row) => { setEditing(row); setShowForm(true); } : undefined}
        remove={(row) => { if (window.confirm(row.employeeName + ' を削除しますか？')) void task.run(async () => {
          await deleteEmployee(chapter.id, row.employeeId!, secure); setRows(await employees(chapter.id)); task.setMessage('削除しました');
        }); }} />
    </section>
    {showForm && writable && <EmployeeForm key={editing?.employeeId ?? 'new'} employee={editing} busy={task.busy} cancel={() => setShowForm(false)} save={(value) => void task.run(async () => {
      await saveEmployee(chapter.id, value, secure); setShowForm(false); setRows(await employees(chapter.id)); task.setMessage('保存しました');
    })} />}
    {chapter.id === 'graphql' && <GraphqlPanel />}
    {chapter.id === 'webflux' && <StreamPanel />}
  </>;
}
