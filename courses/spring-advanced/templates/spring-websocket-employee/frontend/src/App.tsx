import { useEffect, useState } from 'react';
import { chapters } from './chapters';
import type { Trace } from './types';
import { observeTrace } from './services/api';
import EmployeeWorkspace from './components/EmployeeWorkspace';
import EventPanel from './components/EventPanel';
import NotificationPanel from './components/NotificationPanel';
import ResiliencePanel from './components/ResiliencePanel';

const chapter = chapters[0];

export default function App() {
  const [trace, setTrace] = useState<Trace | null>(null);
  useEffect(() => observeTrace(setTrace), []);
  return <div className="app">
    <aside><div className="brand"><span className="brand-mark">E</span><div>Employee Lab<small>Spring Boot 発展編</small></div></div>
      <p className="aside-note">この画面は「03 · 社員通知 / STOMP」専用です。<br />対象バックエンドを起動して操作してください。</p>
    </aside>
    <main>
      <header><span className="eyebrow">EMPLOYEE MANAGEMENT / ADVANCED</span><h1>{chapter.title}</h1><p>{chapter.description}</p></header>
      {new URLSearchParams(location.search).has('loginError') && <p role="alert" className="error">OIDCログインに失敗しました。クライアント設定・リダイレクトURIを確認してください。</p>}
      <div>
        {chapter.kind === 'employees' && <EmployeeWorkspace chapter={chapter} />}
        {chapter.kind === 'events' && <EventPanel chapter={chapter} />}
        {chapter.kind === 'notifications' && <NotificationPanel />}
        {chapter.kind === 'resilience' && <ResiliencePanel chapter={chapter} />}
      </div>
      <section className="panel trace"><h2>直近のHTTP応答</h2>
        {trace ? <><p><code>{trace.method} {trace.path}</code><span className={trace.status >= 400 ? 'badge bad' : 'badge'}>{trace.status}</span><small>{trace.elapsed} ms</small></p><pre>{JSON.stringify(trace.body, null, 2)}</pre></> : <p className="muted">操作すると、実際のステータス・応答時間・レスポンスがここに表示されます。</p>}
      </section>
      <footer>学習用ローカルアプリ · バックエンド 8096 専用</footer>
    </main>
  </div>;
}
