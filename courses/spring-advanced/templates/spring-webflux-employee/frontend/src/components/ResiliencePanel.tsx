import { useState } from 'react';
import type { Chapter, Employee } from '../types';
import { request } from '../services/api';
import { useTask } from './useTask';
import { Feedback } from './Feedback';
import EmployeeTable from './EmployeeTable';
interface Result { employees: Employee[]; fallback: boolean; message: string }
interface Circuit { state: string; failedCalls: number; failureRate: number }
export default function ResiliencePanel({ chapter }: { chapter: Chapter }) {
  const [result, setResult] = useState<Result | null>(null);
  const [circuit, setCircuit] = useState<Circuit | null>(null);
  const task = useTask();
  return <section className="panel"><h2>社員APIの障害を観察する</h2>
    <ol><li>社員REST APIを起動して一覧取得を試します。</li><li>自分で起動した社員REST APIを停止して、同じ操作を繰り返します。</li>
      <li>APIを再起動し、Retryは再実行、Circuit Breakerは10秒待って複数回実行し回復を確認します。</li></ol>
    <div className="inline">
      <button disabled={task.busy} onClick={() => void task.run(async () => {
        setResult(await request<Result>(chapter.id, chapter.id === 'retry' ? '/retry-demo' : '/resilience-demo'));
        if (chapter.id === 'resilience') setCircuit(await request<Circuit>(chapter.id, '/resilience-status'));
      })}>{task.busy ? '応答を待っています…' : '社員一覧を取得'}</button>
      {chapter.id === 'resilience' && <>
        <button disabled={task.busy} onClick={() => void task.run(async () => { setCircuit(await request<Circuit>(chapter.id, '/resilience-status')); })}>回路状態を取得</button>
        <button disabled={task.busy} onClick={() => void task.run(async () => {
          const results = await Promise.allSettled(Array.from({ length: 8 }, () => request<Employee>(chapter.id, '/ratelimit-demo')));
          task.setMessage(results.map((value, index) => (index + 1) + ': ' + (value.status === 'fulfilled' ? '200' : String(value.reason))).join('\n'));
        })}>8回連続実行（レート制限）</button>
      </>}
    </div>
    <Feedback {...task} />
    {circuit && <p className="state">回路：{circuit.state} / 失敗 {circuit.failedCalls} 回 / 失敗率 {circuit.failureRate < 0 ? '未集計' : circuit.failureRate + '%'}</p>}
    {result && <><p className={result.fallback ? 'error' : 'success'}>{result.fallback ? '代替応答' : '通常応答'}：{result.message}</p><EmployeeTable rows={result.employees} /></>}
  </section>;
}
