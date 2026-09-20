import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import './index.css';

// index.html の <div id="root"> に React の世界をぶら下げる
const container = document.getElementById('root');
if (!container) throw new Error('#root が見つかりません');

createRoot(container).render(
  // StrictMode は開発中だけ、副作用の書き方の誤りを見つけるために二重に呼ぶ
  <StrictMode>
    <App />
  </StrictMode>,
);
