// 画面をまたいで使う型はここにまとめる

export type Department = '営業部' | '企画部' | '人事部';

export interface Person {
  id: number;
  name: string;
  department: Department;
}

export const DEPARTMENTS: Department[] = ['営業部', '企画部', '人事部'];
