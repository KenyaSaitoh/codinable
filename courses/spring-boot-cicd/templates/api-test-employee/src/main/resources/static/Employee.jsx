const { useEffect, useState } = React;

const emptyEmployee = {
  employeeId: '', employeeName: '', departmentId: '', jobId: '',
  salary: '', entranceDate: '', version: null
};
const emptySearch = { keyword: '', departmentId: '', jobId: '', salaryFrom: '', salaryTo: '' };

async function request(url, options = {}) {
  const response = await fetch(url, options);
  const body = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${body.message || '操作に失敗しました'} ${
      (body.errors || []).join(' / ')}`);
  }
  return body;
}

const EmployeeManager = () => {
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [editingEmployee, setEditingEmployee] = useState(emptyEmployee);
  const [search, setSearch] = useState(emptySearch);
  const [activeSearch, setActiveSearch] = useState(emptySearch);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  const loadEmployees = async (nextPage, criteria = activeSearch) => {
    const params = new URLSearchParams({ page: String(nextPage) });
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== '') params.set(key, value);
    });
    const result = await request('/employees?' + params);
    if (nextPage > Math.max(1, result.totalPages)) {
      return loadEmployees(Math.max(1, result.totalPages), criteria);
    }
    setEmployees(result.content);
    setPage(nextPage);
    setTotalPages(result.totalPages);
    setTotalElements(result.totalElements);
  };

  const perform = async (action) => {
    setBusy(true);
    setMessage('');
    try { await action(); } catch (error) { setMessage(error.message); }
    finally { setBusy(false); }
  };

  useEffect(() => {
    void perform(async () => {
      const [departmentData, jobData] = await Promise.all([
        request('/departments'), request('/jobs')
      ]);
      setDepartments(departmentData);
      setJobs(jobData);
      await loadEmployees(1, emptySearch);
    });
  }, []);

  const updateField = (event) => {
    setEditingEmployee({ ...editingEmployee, [event.target.name]: event.target.value });
  };

  const save = (event) => {
    event.preventDefault();
    void perform(async () => {
      const employeeId = editingEmployee.employeeId;
      const body = {
        employeeName: editingEmployee.employeeName,
        departmentId: Number(editingEmployee.departmentId),
        jobId: Number(editingEmployee.jobId),
        salary: Number(editingEmployee.salary),
        entranceDate: editingEmployee.entranceDate
      };
      if (employeeId) body.version = editingEmployee.version;
      await request(employeeId ? `/employees/${employeeId}` : '/employees', {
        method: employeeId ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
      });
      setEditingEmployee(emptyEmployee);
      await loadEmployees(page);
      setMessage(employeeId ? '社員を更新しました。' : '社員を登録しました。');
    });
  };

  const remove = (employeeId) => {
    void perform(async () => {
      await request(`/employees/${employeeId}`, { method: 'DELETE' });
      if (editingEmployee.employeeId === employeeId) setEditingEmployee(emptyEmployee);
      await loadEmployees(page);
      setMessage('社員を論理削除しました。');
    });
  };

  const departmentName = (id) => departments.find((item) => item.departmentId === id)?.departmentName;
  const jobName = (id) => jobs.find((item) => item.jobId === id)?.jobName;
  const updateSearch = (event) => setSearch({ ...search, [event.target.name]: event.target.value });

  return <main>
    <h2>社員管理システム（JPA REST API）</h2>
    <p role="status">{message}</p>
    <form onSubmit={save}>
      <fieldset disabled={busy}>
        <legend>{editingEmployee.employeeId ? '社員を編集' : '社員を登録'}</legend>
        <label>氏名 <input name="employeeName" required maxLength={30}
          value={editingEmployee.employeeName} onChange={updateField} /></label>{' '}
        <label htmlFor="employeeDepartment">部署 </label>
        <select id="employeeDepartment" name="departmentId" required value={editingEmployee.departmentId} onChange={updateField}>
          <option value="">選択してください</option>
          {departments.map((item) => <option key={item.departmentId} value={item.departmentId}>{item.departmentName}</option>)}
        </select>{' '}
        <label htmlFor="employeeJob">役職 </label>
        <select id="employeeJob" name="jobId" required value={editingEmployee.jobId} onChange={updateField}>
          <option value="">選択してください</option>
          {jobs.map((item) => <option key={item.jobId} value={item.jobId}>{item.jobName}</option>)}
        </select>{' '}
        <label>月給 <input name="salary" type="number" required min={0} max={9999999} step={1}
          value={editingEmployee.salary} onChange={updateField} /></label>{' '}
        <label>入社日 <input name="entranceDate" type="date" required
          value={editingEmployee.entranceDate} onChange={updateField} /></label>{' '}
        <button type="submit">{editingEmployee.employeeId ? '更新' : '登録'}</button>{' '}
        <button type="button" onClick={() => setEditingEmployee(emptyEmployee)}>クリア</button>
      </fieldset>
    </form>
    <form onSubmit={(event) => {
      event.preventDefault();
      void perform(async () => { await loadEmployees(1, search); setActiveSearch(search); });
    }}>
      <fieldset disabled={busy}>
        <legend>社員を検索</legend>
        <label>氏名 <input name="keyword" value={search.keyword} onChange={updateSearch} /></label>{' '}
        <label htmlFor="searchDepartment">部署 </label>
        <select id="searchDepartment" name="departmentId" value={search.departmentId} onChange={updateSearch}>
          <option value="">すべて</option>
          {departments.map((item) => <option key={item.departmentId} value={item.departmentId}>{item.departmentName}</option>)}
        </select>{' '}
        <label htmlFor="searchJob">役職 </label>
        <select id="searchJob" name="jobId" value={search.jobId} onChange={updateSearch}>
          <option value="">すべて</option>
          {jobs.map((item) => <option key={item.jobId} value={item.jobId}>{item.jobName}</option>)}
        </select>{' '}
        <label>月給下限 <input name="salaryFrom" type="number" min={0} max={9999999}
          value={search.salaryFrom} onChange={updateSearch} /></label>{' '}
        <label>月給上限 <input name="salaryTo" type="number" min={0} max={9999999}
          value={search.salaryTo} onChange={updateSearch} /></label>{' '}
        <button type="submit">検索</button>
      </fieldset>
    </form>
    <p>{totalElements}件 / {page}ページ（全{Math.max(1, totalPages)}ページ）</p>
    <button type="button" disabled={busy || page <= 1}
      onClick={() => void perform(() => loadEmployees(page - 1))}>前へ</button>{' '}
    <button type="button" disabled={busy || page >= totalPages}
      onClick={() => void perform(() => loadEmployees(page + 1))}>次へ</button>
    <table border="1">
      <thead><tr><th>社員コード</th><th>氏名</th><th>部署</th><th>役職</th><th>月給</th><th>入社日</th><th>操作</th></tr></thead>
      <tbody>{employees.map((employee) => <tr key={employee.employeeId}>
        <td>{employee.employeeCode}</td><td>{employee.employeeName}</td>
        <td>{departmentName(employee.departmentId)}</td><td>{jobName(employee.jobId)}</td>
        <td>{employee.salary}</td><td>{employee.entranceDate}</td>
        <td><button type="button" disabled={busy} onClick={() => setEditingEmployee(employee)}>編集</button>{' '}
          <button type="button" disabled={busy} onClick={() => remove(employee.employeeId)}>削除</button></td>
      </tr>)}</tbody>
    </table>
  </main>;
};
