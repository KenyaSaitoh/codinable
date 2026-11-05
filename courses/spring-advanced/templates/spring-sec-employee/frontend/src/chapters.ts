import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'security', title: '04 · フォーム認証', description: 'フォーム認証・認可・CSRF保護を確認します。', project: '', port: 8083, kind: 'employees' },
];

export const secured = (chapterId: string) => chapterId === 'security';
export const readOnly = (chapterId: string) => ['restclient', 'resttemplate', 'webclient'].includes(chapterId);
