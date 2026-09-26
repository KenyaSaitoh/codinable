import type { Chapter } from './types';

export const chapters: Chapter[] = [
  { id: 'keycloak', title: '04 · Keycloak OIDC', description: 'KeycloakのIDトークンによる認可を確認します。', project: '', port: 8085, kind: 'employees' },
];

export const secured = (chapterId: string) => ['security', 'google', 'keycloak'].includes(chapterId);
export const readOnly = (chapterId: string) => ['google', 'restclient', 'resttemplate', 'webclient'].includes(chapterId);
