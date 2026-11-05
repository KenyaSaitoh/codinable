import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'resilience', title: '09 · Circuit Breaker', description: '回路状態・代替応答・レート制限を確認します。', project: '', port: 8082, kind: 'resilience' },
];

export const secured = (chapterId: string) => chapterId === 'security';
export const readOnly = (chapterId: string) => ['restclient', 'resttemplate', 'webclient'].includes(chapterId);
