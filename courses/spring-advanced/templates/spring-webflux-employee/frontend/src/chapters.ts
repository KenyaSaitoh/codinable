import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'webflux', title: '08 · WebFlux / SSE', description: 'Reactive CRUDとSSEを確認します。', project: '', port: 8089, kind: 'employees' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
