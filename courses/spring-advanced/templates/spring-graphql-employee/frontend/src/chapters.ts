import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'graphql', title: '07 · GraphQL', description: 'Query・Mutation・ネスト取得を確認します。', project: '', port: 8088, kind: 'employees' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
