import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'websocket', title: '03 · 社員通知 / STOMP', description: 'STOMPで社員通知を送受信します。', project: '', port: 8096, kind: 'notifications' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
