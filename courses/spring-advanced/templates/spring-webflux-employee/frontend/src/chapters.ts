import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'webflux', title: '07 · WebFlux / SSE', description: 'Reactive CRUDとSSEを確認します。', project: '', port: 8089, kind: 'employees' },
];

export const secured = (chapterId: string) => chapterId === 'security';
export const readOnly = (chapterId: string) => ['restclient', 'resttemplate', 'webclient'].includes(chapterId);
