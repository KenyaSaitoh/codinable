// 社員API の呼び出し。要求はすべて同じオリジンの /api へ送り、Vite が Payara Server の
// /jakarta-security-employee-google/api へ転送する。認証はセッション Cookie（GOOGLE_SESSION）だけで、
// Webブラウザは ID トークンもアクセストークンも持たない

export type Employee = {
  employeeId: number;
  employeeName: string;
  departmentId: number | null;
  departmentName: string | null;
  jobName: string | null;
  salary: number;
  entranceDate: string | null;
};

export type Session = { name: string; subject: string; email: string; roles: string[] };

// 「Googleでログイン」で開く保護された入口。未ログインなら Google のログイン画面へ転送される
// （OIDC のコールバックは 8080 で登録するので、入口は 8080 の localhost にそろえる）
export const LOGIN_URL = 'http://localhost:8080/jakarta-security-employee-google/login';

export class HttpError extends Error {
  constructor(public status: number) {
    super(`HTTP ${status}`);
  }
}

async function send(path: string, init: RequestInit = {}): Promise<Response> {
  // 未ログインで保護された API を呼ぶと、認証メカニズムが Google へのリダイレクトを返す。追わずに未認証として扱う
  const res = await fetch(path, { ...init, redirect: 'manual' });
  if (res.type === 'opaqueredirect') throw new HttpError(401);
  if (!res.ok) throw new HttpError(res.status);
  return res;
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

export async function deleteEmployee(id: number): Promise<void> {
  await send(`/api/employees/${id}`, { method: 'DELETE' });
}
