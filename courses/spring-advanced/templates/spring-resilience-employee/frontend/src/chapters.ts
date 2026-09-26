import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'resilience', title: '10 · Circuit Breaker', description: '回路状態・代替応答・レート制限を確認します。', project: '', port: 8082, kind: 'resilience' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
