import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'amqp', title: '09 · RabbitMQ', description: 'イベント送信とコンシューマーの受信履歴を確認します。', project: '', port: 8093, kind: 'events' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
