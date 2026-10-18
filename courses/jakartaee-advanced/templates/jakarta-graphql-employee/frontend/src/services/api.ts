// GraphQL の要求。どの操作も同じオリジンの /graphql へ POST し、Vite が Payara Server の
// /jakarta-graphql-employee/graphql へ転送する。HTTP 200 でも本文の errors を必ず確かめる

export type Department = { id: string; departmentName: string; employees?: { id: string; employeeName: string }[] };
export type Employee = {
  id: string;
  employeeName: string;
  jobName?: string | null;
  salary?: number | null;
  entranceDate?: string | null;
  department?: { id: string; departmentName: string } | null;
};

type GraphQLResponse<T> = { data?: T; errors?: { message: string; path?: string[] }[] };

export async function graphql<T>(query: string, variables: Record<string, unknown> = {}): Promise<T> {
  const res = await fetch('/graphql', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, variables }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const body = (await res.json()) as GraphQLResponse<T>;
  // errors が 1 つでもあれば例外として扱う（部分的な結果を表示し分けることはしない）
  if (body.errors?.length) throw new Error(body.errors.map((e) => e.message).join(' / '));
  return body.data as T;
}

export const EMPLOYEE_DETAIL = `
query EmployeeDetail($id: ID!) {
  employee(id: $id) {
    id
    employeeName
    department {
      id
      departmentName
    }
  }
}`;

export async function employeeDetail(id: string): Promise<Employee> {
  return (await graphql<{ employee: Employee }>(EMPLOYEE_DETAIL, { id })).employee;
}

export async function employees(): Promise<Employee[]> {
  return (await graphql<{ employees: Employee[] }>(
    '{ employees { id employeeName jobName salary entranceDate department { departmentName } } }')).employees;
}

export async function createEmployee(input: Record<string, unknown>): Promise<Employee> {
  return (await graphql<{ createEmployee: Employee }>(
    'mutation Create($input: EmployeeInput!) { createEmployee(input: $input) { id employeeName } }',
    { input })).createEmployee;
}

export async function deleteEmployee(id: string): Promise<string> {
  return (await graphql<{ deleteEmployee: string }>(
    'mutation Delete($id: ID!) { deleteEmployee(id: $id) }', { id })).deleteEmployee;
}
