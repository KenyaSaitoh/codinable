import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'google', title: '04 · Google OIDC', description: 'Googleで本人確認し、社員一覧を閲覧します。', project: '', port: 8084, kind: 'employees' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
