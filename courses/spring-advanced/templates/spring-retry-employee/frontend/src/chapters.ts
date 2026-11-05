import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'retry', title: '09 · Retry', description: 'リトライ・バックオフ・代替応答を確認します。', project: '', port: 8081, kind: 'resilience' },
];

export const secured = (chapterId: string) => chapterId === 'security';
export const readOnly = (chapterId: string) => ['restclient', 'resttemplate', 'webclient'].includes(chapterId);
