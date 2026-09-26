import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'security', title: '06 · フォーム認証', description: 'フォーム認証・認可・CSRF保護を確認します。', project: '', port: 8083, kind: 'employees' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
