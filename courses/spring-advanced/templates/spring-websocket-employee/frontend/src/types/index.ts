export interface Employee {
  employeeId: number | null;
  employeeName: string;
  departmentId: number;
  departmentName: string | null;
  jobName: string | null;
  salary: number;
  entranceDate: string | null;
}
export interface Session { name: string; authorities: string[] }
export interface Csrf { token: string; headerName: string }
export interface EmployeeEvent { employeeId: number; employeeName: string; eventType: string; occurredAt: string }
export interface Notice { employeeId: number; employeeName: string; message: string; occurredAt: string }
export interface Trace { method: string; path: string; status: number; elapsed: number; body: unknown }
export type ChapterId = 'rest' | 'security' | 'google' | 'keycloak' | 'restclient' | 'resttemplate' | 'webclient' | 'graphql' | 'webflux' | 'websocket' | 'kafka' | 'amqp' | 'retry' | 'resilience';
export interface Chapter { id: ChapterId; title: string; description: string; project: string; port: number; kind: 'employees' | 'notifications' | 'events' | 'resilience' }
