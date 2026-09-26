import { useState } from 'react';
import type { Chapter, EmployeeEvent } from '../types';
import { request, write } from '../services/api';
import { useTask } from './useTask';
import { Feedback } from './Feedback';
export default function EventPanel({ chapter }: { chapter: Chapter }) {
  const [employeeId, setEmployeeId] = useState(1);
  const [employeeName, setEmployeeName] = useState('Alice');
  const [eventType, setEventType] = useState('EMPLOYEE_CREATED');
  const [events, setEvents] = useState<EmployeeEvent[]>([]);
  const task = useTask();
  return <section className="panel"><h2>社員イベントの送信と受信</h2>
    <p>社員データのCRUDとは独立したイベント送信実験です。履歴はコンシューマーが受信した直近100件で、再起動すると消えます。</p>
    <form className="inline" onSubmit={(event) => { event.preventDefault(); void task.run(async () => {
      await write(chapter.id, '/events', 'POST', { employeeId, employeeName, eventType, occurredAt: new Date().toISOString().replace('Z', '') });
      task.setMessage('送信を受理しました（202）。「受信履歴を取得」でコンシューマー側の到着を確認してください。');
    }); }}>
      <label>社員ID<input type="number" required min={1} value={employeeId} onChange={(event) => setEmployeeId(Number(event.target.value))} /></label>
      <label>社員名<input required maxLength={20} value={employeeName} onChange={(event) => setEmployeeName(event.target.value)} /></label>
      <label>イベント<select value={eventType} onChange={(event) => setEventType(event.target.value)}>
        <option>EMPLOYEE_CREATED</option><option>EMPLOYEE_UPDATED</option><option>EMPLOYEE_DELETED</option>
      </select></label><button disabled={task.busy}>イベント送信</button>
    </form><Feedback {...task} />
    <button disabled={task.busy} onClick={() => void task.run(async () => { setEvents(await request<EmployeeEvent[]>(chapter.id + '-consumer', '/events')); })}>受信履歴を取得</button>
    <p role="status">{events.length} 件受信</p>
    <div className="table-wrap"><table><thead><tr><th>発生日時（UTC）</th><th>イベント</th><th>社員</th></tr></thead>
      <tbody>{events.map((event, index) => <tr key={index}><td>{event.occurredAt}</td><td>{event.eventType}</td><td>#{event.employeeId} {event.employeeName}</td></tr>)}</tbody>
    </table></div>
  </section>;
}
