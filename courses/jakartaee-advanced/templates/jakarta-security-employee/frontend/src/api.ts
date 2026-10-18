// 社員API の呼び出し。要求はすべて同じオリジンの /api へ送り、Vite が Payara Server へ転送する
// 変更要求（POST・PUT・DELETE）の直前に GET /api/csrf でトークンを取り、返された headerName のヘッダーに付ける

export type Employee = {
  employeeId: number;
  employeeName: string;
  departmentId: number | null;
  departmentName: string | null;
  jobName: string | null;
  salary: number;
  entranceDate: string | null;
};

export type Session = { username: string; roles: string[] };

export class HttpError extends Error {
  constructor(public status: number) {
    super(`HTTP ${status}`);
  }
}

async function csrfHeader(): Promise<Record<string, string>> {
  const res = await fetch('/api/csrf');
  if (!res.ok) throw new HttpError(res.status);
  const { headerName, token } = (await res.json()) as { headerName: string; token: string };
  return { [headerName]: token };
}

async function send(path: string, init: RequestInit = {}): Promise<Response> {
  const method = (init.method ?? 'GET').toUpperCase();
  const headers = new Headers(init.headers);
  if (method !== 'GET') {
    Object.entries(await csrfHeader()).forEach(([k, v]) => headers.set(k, v));
  }
  const res = await fetch(path, { ...init, headers });
  if (!res.ok) throw new HttpError(res.status);
  return res;
}

export async function login(username: string, password: string): Promise<void> {
  await send('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ username, password }),
  });
}

export async function logout(): Promise<void> {
  await send('/api/logout', { method: 'POST' });
}

export async function session(): Promise<Session> {
  return (await send('/api/session')).json();
}

export async function employees(): Promise<Employee[]> {
  return (await send('/api/employees')).json();
}

export async function createEmployee(input: Omit<Employee, 'employeeId' | 'departmentName'>): Promise<Employee> {
  return (await send('/api/employees', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  })).json();
}

export async function deleteEmployee(id: number): Promise<void> {
  await send(`/api/employees/${id}`, { method: 'DELETE' });
}
