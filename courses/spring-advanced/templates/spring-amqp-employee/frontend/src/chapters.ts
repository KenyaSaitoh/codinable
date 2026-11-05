import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'amqp', title: '08 · RabbitMQ', description: 'イベント送信とコンシューマーの受信履歴を確認します。', project: '', port: 8093, kind: 'events' },
];

export const secured = (chapterId: string) => chapterId === 'security';
export const readOnly = (chapterId: string) => ['restclient', 'resttemplate', 'webclient'].includes(chapterId);
