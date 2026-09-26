import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'retry', title: '11 · Retry', description: 'リトライ・バックオフ・代替応答を確認します。', project: '', port: 8081, kind: 'resilience' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
