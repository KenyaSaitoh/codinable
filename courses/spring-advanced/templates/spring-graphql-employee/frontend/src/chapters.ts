import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'graphql', title: '06 · GraphQL', description: 'Query・Mutation・ネスト取得を確認します。', project: '', port: 8088, kind: 'employees' },
];

export const secured = (chapterId: string) => chapterId === 'security';
export const readOnly = (chapterId: string) => ['restclient', 'resttemplate', 'webclient'].includes(chapterId);
