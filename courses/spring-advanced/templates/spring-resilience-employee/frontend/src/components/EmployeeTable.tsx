import type { Employee } from '../types';
export default function EmployeeTable({ rows, edit, remove }: { rows: Employee[]; edit?: (row: Employee) => void; remove?: (row: Employee) => void }) {
  return <div className="table-wrap"><table><thead><tr>
    <th>ID</th><th>社員名</th><th>部署</th><th>職種</th><th>給与</th><th>入社日</th>{edit && <th>操作</th>}
  </tr></thead><tbody>{rows.map((row) => <tr key={row.employeeId}>
    <td>{row.employeeId}</td><td>{row.employeeName}</td><td>{row.departmentName || row.departmentId}</td>
    <td>{row.jobName}</td><td>{row.salary?.toLocaleString('ja-JP')} 円</td><td>{row.entranceDate}</td>
    {edit && <td className="actions"><button onClick={() => edit(row)}>編集</button><button className="danger" onClick={() => remove?.(row)}>削除</button></td>}
  </tr>)}</tbody></table>{rows.length === 0 && <p className="muted empty">表示する社員はありません。取得ボタンでデータを読み込んでください。</p>}</div>;
}
