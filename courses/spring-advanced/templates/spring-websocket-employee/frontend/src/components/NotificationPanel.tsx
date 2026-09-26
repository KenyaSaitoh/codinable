import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import type { Notice } from '../types';
export default function NotificationPanel() {
  const connection = useRef<Client | null>(null);
  const [connected, setConnected] = useState(false);
  const [status, setStatus] = useState('未接続');
  const [employeeId, setEmployeeId] = useState(1);
  const [employeeName, setEmployeeName] = useState('Alice');
  const [message, setMessage] = useState('開発部へ異動しました');
  const [notices, setNotices] = useState<Notice[]>([]);
  useEffect(() => () => { void connection.current?.deactivate(); }, []);
  function connect() {
    if (connection.current?.active) return;
    setStatus('接続中');
    const client = new Client({
      brokerURL: location.origin.replace(/^http/, 'ws') + '/api/websocket/ws',
      reconnectDelay: 0,
      onConnect: () => {
        setConnected(true); setStatus('接続済み');
        client.subscribe('/topic/employees', (frame) => {
          try { const notice = JSON.parse(frame.body) as Notice; setNotices((previous) => [notice, ...previous].slice(0, 100)); }
          catch { setStatus('受信データを読み取れません'); }
        });
        client.subscribe('/user/queue/errors', (frame) => setStatus(frame.body));
      },
      onWebSocketClose: () => { setConnected(false); setStatus('切断されました'); },
      onStompError: (frame) => setStatus(frame.headers.message || 'STOMPエラー'),
      onWebSocketError: () => setStatus('接続に失敗しました。バックエンドを確認してください'),
    });
    connection.current = client; client.activate();
  }
  return <section className="panel"><h2>社員通知</h2>
    <div className="inline"><button disabled={connected} onClick={connect}>接続</button>
      <button onClick={() => { void connection.current?.deactivate(); setConnected(false); }}>切断</button><span role="status">{status}</span></div>
    <form className="form-grid" onSubmit={(event) => { event.preventDefault();
      if (connection.current?.connected) connection.current.publish({ destination: '/app/employees/notifications', body: JSON.stringify({ employeeId, employeeName, message }) });
    }}>
      <label>社員ID<input type="number" required min={1} value={employeeId} onChange={(event) => setEmployeeId(Number(event.target.value))} /></label>
      <label>社員名<input required maxLength={20} value={employeeName} onChange={(event) => setEmployeeName(event.target.value)} /></label>
      <label className="wide">通知内容<input required maxLength={200} value={message} onChange={(event) => setMessage(event.target.value)} /></label>
      <button disabled={!connected}>通知を送信</button>
    </form>
    <ul className="notices">{notices.map((notice, index) => <li key={index}><small>{notice.occurredAt}</small><p>#{notice.employeeId} {notice.employeeName} · {notice.message}</p></li>)}</ul>
  </section>;
}
