import { useState } from 'react';
import { graphql, type Department } from '../services/api';

// 部署とその所属社員をまとめて取得するクエリ
// （Department.employees は、サーバーの @Source List<Department> のバッチで解決される）
const DEPARTMENTS_WITH_EMPLOYEES = `
query DepartmentsWithEmployees {
  departments {
    id
    departmentName
    employees {
      id
      employeeName
    }
  }
}`;

export default function GraphqlPanel() {
  const [departments, setDepartments] = useState<Department[] | null>(null);
  const [error, setError] = useState('');

  const run = async () => {
    try {
      setError('');
      setDepartments((await graphql<{ departments: Department[] }>(DEPARTMENTS_WITH_EMPLOYEES)).departments);
    } catch (e) {
      setError(String(e));
    }
  };

  return (
    <section className="panel">
      <h2>部署と所属社員</h2>
      <pre>{DEPARTMENTS_WITH_EMPLOYEES.trim()}</pre>
      <button onClick={run}>実行</button>
      {error && <p className="message">{error}</p>}
      {departments && (
        <ul>
          {departments.map((d) => (
            <li key={d.id}>
              {d.id} {d.departmentName}: {d.employees?.map((e) => e.employeeName).join(', ') || '（なし）'}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
