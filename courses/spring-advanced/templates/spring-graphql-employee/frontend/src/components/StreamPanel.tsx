import { useEffect, useRef, useState } from 'react';
import type { Employee } from '../types';
import EmployeeTable from './EmployeeTable';
export default function StreamPanel() {
  const connection = useRef<EventSource | null>(null);
  const [rows, setRows] = useState<Employee[]>([]);
  const [status, setStatus] = useState('未接続');
  function stop() { connection.current?.close(); connection.current = null; setStatus('停止'); }
  useEffect(() => () => connection.current?.close(), []);
  function start() {
    connection.current?.close(); setRows([]); setStatus('配信中');
    const source = new EventSource('/api/webflux/employees/stream');
    connection.current = source;
    source.onmessage = (event) => {
      try { const row = JSON.parse(event.data) as Employee; setRows((previous) => [...previous, row]); }
      catch { source.close(); setStatus('受信データを読み取れません'); }
    };
    source.addEventListener('complete', () => { source.close(); setStatus('配信完了'); });
    source.onerror = () => { source.close(); setStatus('接続エラー。バックエンドの起動状態を確認してください'); };
  }
  return <section className="panel"><h2>社員のストリーミング配信</h2>
    <div className="inline"><button onClick={start}>配信開始</button><button onClick={stop}>配信停止</button><span role="status">{status} · {rows.length} 件受信</span></div>
    <EmployeeTable rows={rows} />
  </section>;
}
