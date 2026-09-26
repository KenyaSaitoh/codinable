import { useState } from 'react';
import type { ChapterId, Session } from '../types';
import { login, request, write } from '../services/api';
import { useTask } from './useTask';
import { Feedback } from './Feedback';
export default function SessionPanel({ chapter, session, onSession }: { chapter: ChapterId; session: Session | null; onSession: (value: Session | null) => void }) {
  const [username, setUsername] = useState('Alice');
  const [password, setPassword] = useState('');
  const task = useTask();
  return <section className="panel">
    <h2>ログインと権限</h2>
    <p>{session ? session.name + ' / ' + session.authorities.join(', ') : 'セッション未確認・未ログイン'}</p>
    {chapter === 'security' ? <form className="inline" onSubmit={(event) => { event.preventDefault(); void task.run(async () => {
      await login(chapter, username, password); setPassword(''); onSession(await request<Session>(chapter, '/session'));
    }); }}>
      <label>ユーザー名<input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" required /></label>
      <label>パスワード<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" required /></label>
      <button disabled={task.busy}>ログイン</button>
      <small>教材用：Alice / 1111（閲覧）、Bob / 2222（管理者）</small>
    </form> : <a className="button" href={'/api/' + chapter + '/oauth2/authorization/' + chapter}>{chapter} でログイン</a>}
    <div className="inline">
      <button disabled={task.busy} onClick={() => void task.run(async () => {
        try { onSession(await request<Session>(chapter, '/session')); }
        catch (error) { onSession(null); throw error; }
      })}>セッション確認</button>
      <button disabled={task.busy} onClick={() => void task.run(async () => { await write(chapter, '/logout', 'POST', undefined, true); onSession(null); })}>ログアウト</button>
    </div>
    <Feedback {...task} />
  </section>;
}
