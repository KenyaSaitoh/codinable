import type { ChapterId, Csrf, Employee, Trace } from '../types';
const observers = new Set<(trace: Trace) => void>();
export function observeTrace(observer: (trace: Trace) => void) {
  observers.add(observer);
  return () => { observers.delete(observer); };
}
export async function request<T>(chapter: string, path: string, init: RequestInit = {}): Promise<T> {
  const url = '/api/' + chapter + path;
  const start = performance.now();
  let response: Response;
  try {
    response = await fetch(url, { credentials: 'same-origin', signal: AbortSignal.timeout(20000), ...init });
  } catch {
    throw new Error('接続できません。対象バックエンドの起動と接続先を確認してください。');
  }
  const text = await response.text();
  let body: unknown = text || null;
  try { body = text ? JSON.parse(text) : null; } catch { /* テキスト形式のエラーも表示する */ }
  // 認証・CSRFレスポンスは画面の通信ログに残さない
  if (!['/csrf', '/session', '/login', '/logout'].includes(path)) {
    const trace = { method: init.method || 'GET', path: url, status: response.status, elapsed: Math.round(performance.now() - start), body };
    observers.forEach((observer) => observer(trace));
  }
  if (!response.ok) {
    const detail = typeof body === 'string' ? body : JSON.stringify(body);
    throw new Error('HTTP ' + response.status + ': ' + (detail || response.statusText));
  }
  return body as T;
}
export async function write<T>(chapter: string, path: string, method: string, body?: unknown, csrf = false) {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (csrf) {
    const token = await request<Csrf>(chapter, '/csrf');
    headers[token.headerName] = token.token;
  }
  return request<T>(chapter, path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
}
export async function login(chapter: string, username: string, password: string) {
  const token = await request<Csrf>(chapter, '/csrf');
  await request(chapter, '/login', { method: 'POST', headers: {
    'Content-Type': 'application/x-www-form-urlencoded', [token.headerName]: token.token,
  }, body: new URLSearchParams({ username, password }) });
}
export async function graphql<T>(query: string, variables: Record<string, unknown> = {}): Promise<T> {
  const result = await write<{ data: T; errors?: { message: string }[] }>('graphql', '/graphql', 'POST', { query, variables });
  if (result.errors?.length) throw new Error(result.errors.map((error) => error.message).join('\n'));
  return result.data;
}
type GraphEmployee = Omit<Employee, 'departmentId' | 'departmentName'> & { department: { departmentId: number; departmentName: string } };
const fields = 'employeeId:id employeeName salary jobName entranceDate department { departmentId:id departmentName }';
const flatten = (employee: GraphEmployee): Employee => ({ ...employee, employeeId: Number(employee.employeeId),
  departmentId: Number(employee.department.departmentId), departmentName: employee.department.departmentName });
export async function employees(chapter: ChapterId, filter = '', value = ''): Promise<Employee[]> {
  if (chapter === 'graphql') {
    if (filter === 'id') {
      const data = await graphql<{ employee: GraphEmployee }>('query($id:ID!){ employee(id:$id){ ' + fields + ' } }', { id: value });
      return data.employee ? [flatten(data.employee)] : [];
    }
    const operation = filter === 'department' ? 'employeesByDepartment(departmentId:$id)' : 'employees';
    const query = (filter === 'department' ? 'query($id:ID!)' : 'query') + '{ items:' + operation + '{ ' + fields + ' } }';
    const data = await graphql<{ items: GraphEmployee[] }>(query, filter === 'department' ? { id: value } : {});
    return data.items.map(flatten);
  }
  const path = filter === 'id' ? '/' + encodeURIComponent(value) : filter === 'department' ?
    '/query_by_department?departmentId=' + encodeURIComponent(value) : filter === 'salary' ? '/query_by_salary?' + value : '';
  const data = await request<Employee | Employee[]>(chapter, '/employees' + path);
  return Array.isArray(data) ? data : [data];
}
export async function saveEmployee(chapter: ChapterId, employee: Employee, csrf: boolean) {
  if (chapter === 'graphql') {
    const input = { employeeName: employee.employeeName, departmentId: employee.departmentId, jobName: employee.jobName,
      salary: employee.salary, entranceDate: employee.entranceDate };
    return graphql(employee.employeeId === null ?
      'mutation($input:EmployeeInput!){ createEmployee(input:$input){ id } }' :
      'mutation($id:ID!,$input:EmployeeInput!){ updateEmployee(id:$id,input:$input){ id } }',
    employee.employeeId === null ? { input } : { id: employee.employeeId, input });
  }
  return write(chapter, '/employees' + (employee.employeeId === null ? '' : '/' + employee.employeeId),
    employee.employeeId === null ? 'POST' : 'PUT', employee, csrf);
}
export async function deleteEmployee(chapter: ChapterId, id: number, csrf: boolean) {
  if (chapter === 'graphql') return graphql('mutation($id:ID!){ deleteEmployee(id:$id) }', { id });
  return write(chapter, '/employees/' + id, 'DELETE', undefined, csrf);
}
