import { useState } from 'react';
import type { Employee } from '../types';
const initial: Employee = { employeeId: null, employeeName: '', departmentId: 10, departmentName: '営業部', jobName: '', salary: 300000, entranceDate: '' };
export default function EmployeeForm({ employee, busy, save, cancel }: { employee: Employee | null; busy: boolean; save: (value: Employee) => void; cancel: () => void }) {
  const [value, setValue] = useState<Employee>(employee || initial);
  return <form className="panel form-grid" onSubmit={(event) => { event.preventDefault(); save({ ...value, entranceDate: value.entranceDate || null }); }}>
    <h2>{value.employeeId === null ? '社員の登録' : '社員 #' + value.employeeId + ' の更新'}</h2>
    <label>社員名<input required maxLength={20} value={value.employeeName} onChange={(event) => setValue({ ...value, employeeName: event.target.value })} /></label>
    <label>部署<select value={value.departmentId} onChange={(event) => setValue({ ...value, departmentId: Number(event.target.value), departmentName: event.target.selectedOptions[0].text })}>
      <option value={10}>営業部</option><option value={20}>開発部</option><option value={30}>総務部</option>
    </select></label>
    <label>職種<input maxLength={20} value={value.jobName || ''} onChange={(event) => setValue({ ...value, jobName: event.target.value })} /></label>
    <label>給与（円）<input required type="number" min={0} max={2147483647} value={value.salary} onChange={(event) => setValue({ ...value, salary: Number(event.target.value) })} /></label>
    <label>入社日<input type="date" value={value.entranceDate || ''} onChange={(event) => setValue({ ...value, entranceDate: event.target.value })} /></label>
    <div className="inline"><button disabled={busy}>保存</button><button type="button" onClick={cancel}>閉じる</button></div>
  </form>;
}
