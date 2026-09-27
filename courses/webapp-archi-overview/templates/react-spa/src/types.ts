// 画面をまたいで使う型はここにまとめる

export type Department = 'SALES' | 'PLANNING' | 'HR' | 'PRODUCT';

export interface Person {
  id: number;
  name: string;
  department: Department;
}

export const DEPARTMENTS: Department[] = ['SALES', 'PLANNING', 'HR', 'PRODUCT'];
